package de.hamedtanha.servertoolkit.feature.ssh.data.source

import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidSshPrivateKeySourceFactoryTest {

    @Test
    fun readsSelectedContentThroughContentResolver() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedBytes = "test-private-key".toByteArray()
        val keyFile = temporaryKeyFile(expectedBytes)

        try {
            val source = AndroidSshPrivateKeySourceFactory(
                contentResolver = context.contentResolver,
            ).create(Uri.fromFile(keyFile))

            val result = source.consume {
                useBytes { bytes, size ->
                    bytes.copyOf(size)
                }
            }

            when (result) {
                is SshPrivateKeySourceResult.Success -> {
                    assertArrayEquals(expectedBytes, result.value)
                }

                is SshPrivateKeySourceResult.Failure -> {
                    fail("Expected successful content consumption, found ${result.error}.")
                }
            }
        } finally {
            keyFile.delete()
        }
    }

    @Test
    fun mapsUnavailableContentToStableSourceFailure() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val missingFile = File(
            context.cacheDir,
            "missing-private-key-${System.nanoTime()}",
        )
        val source = AndroidSshPrivateKeySourceFactory(
            contentResolver = context.contentResolver,
        ).create(Uri.fromFile(missingFile))

        val result = source.consume {
            size
        }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.DocumentUnavailable,
            ),
            result,
        )
    }

    @Test
    fun closesDescriptorAfterSuccessfulConsumption() = runBlocking {
        val expectedBytes = "test-private-key".toByteArray()
        val keyFile = temporaryKeyFile(expectedBytes)
        val parcelFileDescriptor = ParcelFileDescriptor.open(
            keyFile,
            ParcelFileDescriptor.MODE_READ_ONLY,
        )
        val descriptor = AssetFileDescriptor(
            parcelFileDescriptor,
            0,
            AssetFileDescriptor.UNKNOWN_LENGTH,
        )
        val source = AndroidSshPrivateKeySourceFactory(
            descriptorOpener = SshPrivateKeyDescriptorOpener { _, _ ->
                descriptor
            },
        ).create(TEST_CONTENT_URI)

        try {
            val result = source.consume {
                size
            }

            assertEquals(
                SshPrivateKeySourceResult.Success(expectedBytes.size),
                result,
            )
            assertFalse(parcelFileDescriptor.fileDescriptor.valid())
        } finally {
            descriptor.close()
            keyFile.delete()
        }
    }

    @Test
    fun coroutineCancellationCancelsAndroidOpeningSignal() = runBlocking {
        val openingStarted = CountDownLatch(1)
        val cancellationObserved = CountDownLatch(1)
        val source = AndroidSshPrivateKeySourceFactory(
            descriptorOpener = SshPrivateKeyDescriptorOpener { _, cancellationSignal ->
                cancellationSignal.setOnCancelListener {
                    cancellationObserved.countDown()
                }
                openingStarted.countDown()

                if (!cancellationObserved.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw IllegalStateException(
                        "Android cancellation signal was not cancelled.",
                    )
                }

                cancellationSignal.throwIfCanceled()
                throw AssertionError("Expected OperationCanceledException")
            },
        ).create(TEST_CONTENT_URI)

        val attempt = async(Dispatchers.Default) {
            source.consume {
                size
            }
        }

        assertTrue(
            "Descriptor opening did not start.",
            openingStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
        )

        attempt.cancel(
            CancellationException("test cancellation"),
        )

        assertTrue(
            "Android cancellation signal was not observed.",
            cancellationObserved.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
        )

        try {
            attempt.await()
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("test cancellation", error.message)
        }
    }

    @Test
    fun providerCancellationIsPreservedAsCoroutineCancellation() = runBlocking {
        val source = AndroidSshPrivateKeySourceFactory(
            descriptorOpener = SshPrivateKeyDescriptorOpener { _, _ ->
                throw OperationCanceledException("provider cancelled")
            },
        ).create(TEST_CONTENT_URI)

        try {
            source.consume {
                size
            }
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals(
                "Private-key document opening was cancelled.",
                error.message,
            )
            assertTrue(error.cause is OperationCanceledException)
        }
    }

    private fun temporaryKeyFile(bytes: ByteArray): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return File.createTempFile(
            "servertoolkit-private-key-",
            ".tmp",
            context.cacheDir,
        ).apply {
            writeBytes(bytes)
        }
    }

    private companion object {
        val TEST_CONTENT_URI: Uri = Uri.parse(
            "content://de.hamedtanha.servertoolkit.test/private-key",
        )

        const val TIMEOUT_SECONDS: Long = 5
    }
}
