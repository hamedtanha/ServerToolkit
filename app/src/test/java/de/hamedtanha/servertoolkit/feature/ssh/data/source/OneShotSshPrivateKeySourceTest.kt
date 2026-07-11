package de.hamedtanha.servertoolkit.feature.ssh.data.source

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OneShotSshPrivateKeySourceTest {

    @Test
    fun `consumes available content once and closes it`() = runTest {
        var closed = false
        var openCount = 0
        val bytes = "private-key".toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                openCount += 1
                SshPrivateKeyContent(
                    inputStream = inputStream,
                    closeAction = {
                        closed = true
                        inputStream.close()
                    },
                )
            },
        )

        val firstResult = source.consume {
            useBytes { sourceBytes, size ->
                String(sourceBytes, 0, size, StandardCharsets.UTF_8)
            }
        }
        val secondResult = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Success("private-key"),
            firstResult,
        )
        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.AlreadyConsumed,
            ),
            secondResult,
        )
        assertEquals(1, openCount)
        assertTrue(closed)
    }

    @Test
    fun `invalidation prevents consumption and is idempotent`() = runTest {
        var openCount = 0
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                openCount += 1
                contentFor(byteArrayOf(1))
            },
        )

        assertTrue(source.invalidate())
        assertFalse(source.invalidate())

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.Invalidated,
            ),
            result,
        )
        assertEquals(0, openCount)
    }

    @Test
    fun `invalidation after consumption does not change consumed state`() = runTest {
        val source = sourceFor(byteArrayOf(1))

        val result = source.consume { size }

        assertEquals(SshPrivateKeySourceResult.Success(1), result)
        assertFalse(source.invalidate())
        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.AlreadyConsumed,
            ),
            source.consume { size },
        )
    }

    @Test
    fun `only one concurrent consumer claims the source`() = runTest {
        val enteredConsumer = CompletableDeferred<Unit>()
        val releaseConsumer = CompletableDeferred<Unit>()
        val source = sourceFor(byteArrayOf(1, 2, 3))

        val first = async {
            source.consume {
                enteredConsumer.complete(Unit)
                releaseConsumer.await()
                size
            }
        }

        enteredConsumer.await()

        val second = async {
            source.consume { size }
        }

        val secondResult = second.await()
        releaseConsumer.complete(Unit)
        val firstResult = first.await()

        assertEquals(SshPrivateKeySourceResult.Success(3), firstResult)
        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.AlreadyConsumed,
            ),
            secondResult,
        )
    }

    @Test
    fun `rejects unavailable content`() = runTest {
        val source = OneShotSshPrivateKeySource(
            contentOpener = { null },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.DocumentUnavailable,
            ),
            result,
        )
    }

    @Test
    fun `rejects empty content and closes it`() = runTest {
        var closed = false
        val inputStream = ByteArrayInputStream(byteArrayOf())
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                SshPrivateKeyContent(
                    inputStream = inputStream,
                    closeAction = {
                        closed = true
                        inputStream.close()
                    },
                )
            },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.EmptyDocument,
            ),
            result,
        )
        assertTrue(closed)
    }

    @Test
    fun `accepts content exactly at the configured size limit`() = runTest {
        val source = sourceFor(
            ByteArray(MAX_SSH_PRIVATE_KEY_SIZE_BYTES) { 1 },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Success(
                MAX_SSH_PRIVATE_KEY_SIZE_BYTES,
            ),
            result,
        )
    }

    @Test
    fun `rejects content larger than the configured size limit`() = runTest {
        val source = sourceFor(
            ByteArray(MAX_SSH_PRIVATE_KEY_SIZE_BYTES + 1) { 1 },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.DocumentTooLarge,
            ),
            result,
        )
    }

    @Test
    fun `maps content opening failures to document unavailable`() = runTest {
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                throw IOException("Simulated open failure")
            },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.DocumentUnavailable,
            ),
            result,
        )
    }

    @Test
    fun `maps stream failures to stable read failure and closes content`() = runTest {
        var closed = false
        val failingStream = object : InputStream() {
            override fun read(): Int {
                throw IOException("Simulated read failure")
            }

            override fun read(
                target: ByteArray,
                offset: Int,
                length: Int,
            ): Int {
                throw IOException("Simulated read failure")
            }
        }
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                SshPrivateKeyContent(
                    inputStream = failingStream,
                    closeAction = {
                        closed = true
                        failingStream.close()
                    },
                )
            },
        )

        val result = source.consume { size }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.ReadFailed,
            ),
            result,
        )
        assertTrue(closed)
    }

    @Test
    fun `maps close failure after successful read and does not invoke consumer`() = runTest {
        var consumerInvoked = false
        val inputStream = ByteArrayInputStream(
            "private-key".toByteArray(StandardCharsets.UTF_8),
        )
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                SshPrivateKeyContent(
                    inputStream = inputStream,
                    closeAction = {
                        inputStream.close()
                        throw IOException("Simulated close failure")
                    },
                )
            },
        )

        val result = source.consume {
            consumerInvoked = true
            size
        }

        assertEquals(
            SshPrivateKeySourceResult.Failure(
                SshPrivateKeySourceError.ReadFailed,
            ),
            result,
        )
        assertFalse(consumerInvoked)
    }

    @Test
    fun `preserves cancellation from content closing and clears loaded material`() = runTest {
        lateinit var capturedBuffer: ByteArray
        var consumerInvoked = false
        val contentBytes = "private-key".toByteArray(StandardCharsets.UTF_8)
        val inputStream = object : InputStream() {
            private var delivered = false

            override fun read(): Int {
                throw UnsupportedOperationException("Single-byte reads are not expected.")
            }

            override fun read(
                target: ByteArray,
                offset: Int,
                length: Int,
            ): Int {
                if (delivered) {
                    return -1
                }

                capturedBuffer = target
                contentBytes.copyInto(
                    destination = target,
                    destinationOffset = offset,
                )
                delivered = true
                return contentBytes.size
            }
        }
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                SshPrivateKeyContent(
                    inputStream = inputStream,
                    closeAction = {
                        throw CancellationException("close cancelled")
                    },
                )
            },
        )

        try {
            source.consume {
                consumerInvoked = true
                size
            }
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("close cancelled", error.message)
        }

        assertFalse(consumerInvoked)
        assertTrue(capturedBuffer.all { it == 0.toByte() })
    }

    @Test
    fun `preserves cancellation from content opening`() = runTest {
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                throw CancellationException("cancelled")
            },
        )

        try {
            source.consume { size }
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }
    }

    @Test
    fun `preserves consumer cancellation and clears material`() = runTest {
        lateinit var capturedBuffer: ByteArray
        val source = sourceFor(
            "private-key".toByteArray(StandardCharsets.UTF_8),
        )

        try {
            source.consume<Unit> {
                useBytes { bytes, _ ->
                    capturedBuffer = bytes
                }
                throw CancellationException("consumer cancelled")
            }
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("consumer cancelled", error.message)
        }

        assertTrue(capturedBuffer.all { it == 0.toByte() })
    }

    @Test
    fun `clears material after successful consumer completion`() = runTest {
        lateinit var capturedBuffer: ByteArray
        val source = sourceFor(
            "private-key".toByteArray(StandardCharsets.UTF_8),
        )

        val result = source.consume {
            useBytes { bytes, size ->
                capturedBuffer = bytes
                size
            }
        }

        assertEquals(
            SshPrivateKeySourceResult.Success("private-key".length),
            result,
        )
        assertTrue(capturedBuffer.all { it == 0.toByte() })
    }

    @Test
    fun `clears material and closes content when consumer fails`() = runTest {
        lateinit var capturedBuffer: ByteArray
        var closed = false
        val inputStream = ByteArrayInputStream(
            "private-key".toByteArray(StandardCharsets.UTF_8),
        )
        val source = OneShotSshPrivateKeySource(
            contentOpener = {
                SshPrivateKeyContent(
                    inputStream = inputStream,
                    closeAction = {
                        closed = true
                        inputStream.close()
                    },
                )
            },
        )

        try {
            source.consume<Unit> {
                useBytes { bytes, _ ->
                    capturedBuffer = bytes
                }
                throw IllegalStateException("Simulated consumer failure")
            }
            fail("Expected consumer failure")
        } catch (error: IllegalStateException) {
            assertEquals("Simulated consumer failure", error.message)
        }

        assertTrue(closed)
        assertTrue(capturedBuffer.all { it == 0.toByte() })
    }

    @Test
    fun `success result redacts consumer value from string representation`() = runTest {
        val secretValue = "derived-secret-value"
        val source = sourceFor(byteArrayOf(1))

        val result = source.consume { secretValue }

        assertFalse(result.toString().contains(secretValue))
    }

    private fun sourceFor(
        bytes: ByteArray,
    ): OneShotSshPrivateKeySource {
        return OneShotSshPrivateKeySource(
            contentOpener = {
                contentFor(bytes)
            },
        )
    }

    private fun contentFor(
        bytes: ByteArray,
    ): SshPrivateKeyContent {
        return SshPrivateKeyContent(
            ByteArrayInputStream(bytes),
        )
    }
}
