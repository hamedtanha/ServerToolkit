package de.hamedtanha.servertoolkit.feature.ssh.data.source

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Converts an ephemeral Android document reference into a project-owned one-shot private-key source.
 *
 * Android document types and owned file resources remain inside the SSH data layer.
 */
internal class AndroidSshPrivateKeySourceFactory(
    private val descriptorOpener: SshPrivateKeyDescriptorOpener,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    constructor(
        contentResolver: ContentResolver,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(
        descriptorOpener = SshPrivateKeyDescriptorOpener { uri, cancellationSignal ->
            contentResolver.openAssetFileDescriptor(
                uri,
                READ_ONLY_MODE,
                cancellationSignal,
            )
        },
        ioDispatcher = ioDispatcher,
    )

    fun create(uri: Uri): SshPrivateKeySource {
        return OneShotSshPrivateKeySource(
            contentOpener = {
                openContent(uri)
            },
        )
    }

    private suspend fun openContent(uri: Uri): SshPrivateKeyContent? {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()

                continuation.invokeOnCancellation {
                    cancellationSignal.cancel()
                }

                try {
                    val descriptor = descriptorOpener.open(
                        uri = uri,
                        cancellationSignal = cancellationSignal,
                    )

                    if (descriptor == null) {
                        continuation.resume(null)
                        return@suspendCancellableCoroutine
                    }

                    if (!continuation.isActive) {
                        descriptor.closeIgnoringFailure()
                        return@suspendCancellableCoroutine
                    }

                    val content = descriptor.toPrivateKeyContent()
                    continuation.resume(content) { _, resource, _ ->
                        resource.closeIgnoringFailure()
                    }
                } catch (error: OperationCanceledException) {
                    val cancellation = CancellationException(
                        "Private-key document opening was cancelled.",
                    )
                    cancellation.initCause(error)
                    continuation.cancel(cancellation)
                } catch (error: Exception) {
                    continuation.resumeWithException(error)
                }
            }
        }
    }
}

internal fun interface SshPrivateKeyDescriptorOpener {

    fun open(
        uri: Uri,
        cancellationSignal: CancellationSignal,
    ): AssetFileDescriptor?
}

private const val READ_ONLY_MODE: String = "r"

private fun AssetFileDescriptor.toPrivateKeyContent(): SshPrivateKeyContent {
    val stream = try {
        createInputStream()
    } catch (error: Exception) {
        closeAfterFailure(error)
        throw error
    }

    return SshPrivateKeyContent(
        inputStream = stream,
        closeAction = {
            closeOwnedResources(
                inputStream = stream,
                descriptor = this,
            )
        },
    )
}

private fun AssetFileDescriptor.closeAfterFailure(openingError: Exception) {
    try {
        close()
    } catch (closeError: Exception) {
        openingError.addSuppressed(closeError)
    }
}

private fun AssetFileDescriptor.closeIgnoringFailure() {
    try {
        close()
    } catch (_: Exception) {
        // Cancellation cleanup must not replace the active cancellation.
    }
}

private fun SshPrivateKeyContent.closeIgnoringFailure() {
    try {
        close()
    } catch (_: Exception) {
        // Prompt-cancellation cleanup cannot report a secondary close failure.
    }
}

private fun closeOwnedResources(
    inputStream: InputStream,
    descriptor: AssetFileDescriptor,
) {
    var failure: Exception? = null

    try {
        inputStream.close()
    } catch (error: Exception) {
        failure = error
    }

    try {
        descriptor.close()
    } catch (error: Exception) {
        val activeFailure = failure
        if (activeFailure == null) {
            failure = error
        } else {
            activeFailure.addSuppressed(error)
        }
    }

    val closeFailure = failure
    if (closeFailure != null) {
        throw closeFailure
    }
}
