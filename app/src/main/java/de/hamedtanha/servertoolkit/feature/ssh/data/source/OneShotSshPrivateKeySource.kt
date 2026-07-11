package de.hamedtanha.servertoolkit.feature.ssh.data.source

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeyMaterial
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource
import java.io.Closeable
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal const val MAX_SSH_PRIVATE_KEY_SIZE_BYTES: Int = 256 * 1024

/**
 * Atomic one-shot private-key source with bounded streaming reads and scoped material cleanup.
 */
internal class OneShotSshPrivateKeySource(
    private val contentOpener: suspend () -> SshPrivateKeyContent?,
    private val maxSizeBytes: Int = MAX_SSH_PRIVATE_KEY_SIZE_BYTES,
) : SshPrivateKeySource {

    private val state = AtomicReference(State.Available)

    init {
        require(maxSizeBytes > 0) {
            "Private-key size limit must be greater than zero."
        }
    }

    override fun invalidate(): Boolean {
        return state.compareAndSet(State.Available, State.Invalidated)
    }

    override suspend fun <T> consume(
        block: suspend SshPrivateKeyMaterial.() -> T,
    ): SshPrivateKeySourceResult<T> {
        val lifecycleError = claimConsumption()
        if (lifecycleError != null) {
            return SshPrivateKeySourceResult.Failure(lifecycleError)
        }

        val material = when (val loadResult = loadMaterial()) {
            is SshPrivateKeySourceResult.Success -> loadResult.value
            is SshPrivateKeySourceResult.Failure -> return loadResult
        }

        return try {
            SshPrivateKeySourceResult.Success(material.block())
        } finally {
            material.clear()
        }
    }

    private fun claimConsumption(): SshPrivateKeySourceError? {
        while (true) {
            when (state.get()) {
                State.Available -> {
                    if (state.compareAndSet(State.Available, State.Consumed)) {
                        return null
                    }
                }

                State.Consumed -> return SshPrivateKeySourceError.AlreadyConsumed
                State.Invalidated -> return SshPrivateKeySourceError.Invalidated
            }
        }
    }

    private suspend fun loadMaterial(): SshPrivateKeySourceResult<SshPrivateKeyMaterial> {
        val content = try {
            contentOpener()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.DocumentUnavailable,
            )
        } ?: return SshPrivateKeySourceResult.Failure(
            SshPrivateKeySourceError.DocumentUnavailable,
        )

        var result: SshPrivateKeySourceResult<SshPrivateKeyMaterial>? = null
        var cancellation: CancellationException? = null

        try {
            result = readMaterial(content.inputStream)
        } catch (error: CancellationException) {
            cancellation = error
            throw error
        } catch (_: Exception) {
            result = SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.ReadFailed,
            )
        } finally {
            try {
                content.close()
            } catch (closeError: CancellationException) {
                when (val currentResult = result) {
                    is SshPrivateKeySourceResult.Success -> currentResult.value.clear()
                    is SshPrivateKeySourceResult.Failure,
                    null,
                    -> Unit
                }

                val activeCancellation = cancellation
                if (activeCancellation != null) {
                    activeCancellation.addSuppressed(closeError)
                } else {
                    throw closeError
                }
            } catch (closeError: Exception) {
                val activeCancellation = cancellation
                if (activeCancellation != null) {
                    activeCancellation.addSuppressed(closeError)
                } else {
                    when (val currentResult = result) {
                        is SshPrivateKeySourceResult.Success -> {
                            currentResult.value.clear()
                            result = SshPrivateKeySourceResult.Failure(
                                SshPrivateKeySourceError.ReadFailed,
                            )
                        }

                        is SshPrivateKeySourceResult.Failure -> Unit
                        null -> {
                            result = SshPrivateKeySourceResult.Failure(
                                SshPrivateKeySourceError.ReadFailed,
                            )
                        }
                    }
                }
            }
        }

        return checkNotNull(result)
    }

    private suspend fun readMaterial(
        inputStream: InputStream,
    ): SshPrivateKeySourceResult<SshPrivateKeyMaterial> {
        val buffer = ByteArray(maxSizeBytes + 1)
        var validSize = 0
        var materialOwnsBuffer = false

        return try {
            while (validSize < buffer.size) {
                currentCoroutineContext().ensureActive()

                val readCount = inputStream.read(
                    buffer,
                    validSize,
                    buffer.size - validSize,
                )

                when {
                    readCount < 0 -> break
                    readCount == 0 -> return SshPrivateKeySourceResult.Failure(
                        SshPrivateKeySourceError.ReadFailed,
                    )
                    else -> validSize += readCount
                }
            }

            currentCoroutineContext().ensureActive()

            when {
                validSize == 0 -> SshPrivateKeySourceResult.Failure(
                    SshPrivateKeySourceError.EmptyDocument,
                )

                validSize > maxSizeBytes -> SshPrivateKeySourceResult.Failure(
                    SshPrivateKeySourceError.DocumentTooLarge,
                )

                else -> {
                    materialOwnsBuffer = true
                    SshPrivateKeySourceResult.Success(
                        SshPrivateKeyMaterial(
                            bytes = buffer,
                            validSize = validSize,
                        ),
                    )
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.ReadFailed,
            )
        } finally {
            if (!materialOwnsBuffer) {
                buffer.fill(0)
            }
        }
    }

    private enum class State {
        Available,
        Consumed,
        Invalidated,
    }
}

/**
 * Data-layer ownership wrapper for opened private-key content.
 *
 * A future Android implementation can close both its derived stream and its descriptor through the
 * supplied close action without exposing either resource through the domain contract.
 */
internal class SshPrivateKeyContent(
    val inputStream: InputStream,
    private val closeAction: () -> Unit = { inputStream.close() },
) : Closeable {

    private val isClosed = AtomicBoolean(false)

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            closeAction()
        }
    }
}
