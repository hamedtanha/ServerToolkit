package de.hamedtanha.servertoolkit.feature.ssh.data.service

import java.nio.charset.StandardCharsets
import java.util.Base64

internal const val MAX_OPENSSH_BCRYPT_KDF_ROUNDS: Long = 64L

/**
 * Validates bounded OpenSSH v1 metadata before SSHJ begins private-key decryption.
 *
 * The private-key document size boundary limits memory consumption. This validator separately limits
 * attacker-controlled bcrypt work before SSHJ receives the key provider input.
 */
internal class OpenSshPrivateKeyMetadataValidator(
    private val maxBcryptKdfRounds: Long =
        MAX_OPENSSH_BCRYPT_KDF_ROUNDS,
) {

    init {
        require(maxBcryptKdfRounds > 0L) {
            "Maximum OpenSSH bcrypt rounds must be greater than zero."
        }
    }

    fun validate(
        privateKeyBytes: ByteArray,
        privateKeySize: Int,
    ) {
        require(privateKeySize in 1..privateKeyBytes.size) {
            "Private-key size must be within the supplied buffer."
        }

        val decodedKey = decodeOpenSshPemPayload(
            privateKeyBytes = privateKeyBytes,
            privateKeySize = privateKeySize,
        )

        try {
            validateDecodedMetadata(decodedKey)
        } finally {
            decodedKey.fill(0)
        }
    }

    private fun validateDecodedMetadata(
        decodedKey: ByteArray,
    ) {
        val reader = OpenSshBinaryReader(decodedKey)
        reader.requireMagic(OPENSSH_AUTH_MAGIC)

        val cipherName = reader.readAsciiString()
        val kdfName = reader.readAsciiString()
        val kdfOptions = reader.readByteString()

        try {
            if (
                cipherName == OPENSSH_NONE &&
                kdfName == OPENSSH_NONE
            ) {
                if (kdfOptions.isNotEmpty()) {
                    throw SshjPrivateKeyAuthenticationException.InvalidKey()
                }

                return
            }

            if (
                cipherName == OPENSSH_NONE ||
                kdfName != OPENSSH_BCRYPT
            ) {
                throw SshjPrivateKeyAuthenticationException.UnsupportedFormat()
            }

            validateBcryptOptions(kdfOptions)
        } finally {
            kdfOptions.fill(0)
        }
    }

    private fun validateBcryptOptions(
        kdfOptions: ByteArray,
    ) {
        val reader = OpenSshBinaryReader(kdfOptions)
        val salt = reader.readByteString()

        try {
            if (salt.isEmpty()) {
                throw SshjPrivateKeyAuthenticationException.InvalidKey()
            }

            val rounds = reader.readUInt32()

            if (reader.hasRemaining) {
                throw SshjPrivateKeyAuthenticationException.InvalidKey()
            }

            if (rounds !in 1L..maxBcryptKdfRounds) {
                throw SshjPrivateKeyAuthenticationException.UnsupportedFormat()
            }
        } finally {
            salt.fill(0)
        }
    }
}

private fun decodeOpenSshPemPayload(
    privateKeyBytes: ByteArray,
    privateKeySize: Int,
): ByteArray {
    val beginIndex = privateKeyBytes.indexOfSequence(
        sequence = OPENSSH_PEM_BEGIN,
        fromIndex = 0,
        limit = privateKeySize,
    )

    if (
        beginIndex < 0 ||
        !privateKeyBytes.isLineStart(beginIndex)
    ) {
        throw SshjPrivateKeyAuthenticationException.InvalidKey()
    }

    val payloadStart = beginIndex + OPENSSH_PEM_BEGIN.size
    val endIndex = privateKeyBytes.indexOfSequence(
        sequence = OPENSSH_PEM_END,
        fromIndex = payloadStart,
        limit = privateKeySize,
    )

    if (endIndex < payloadStart) {
        throw SshjPrivateKeyAuthenticationException.InvalidKey()
    }

    val encodedPayload = ByteArray(endIndex - payloadStart)
    var encodedSize = 0

    try {
        for (index in payloadStart until endIndex) {
            val value = privateKeyBytes[index]

            when {
                value.isBase64Byte() -> {
                    encodedPayload[encodedSize] = value
                    encodedSize += 1
                }

                value.isPemWhitespace() -> Unit

                else -> {
                    throw SshjPrivateKeyAuthenticationException.InvalidKey()
                }
            }
        }

        if (encodedSize == 0) {
            throw SshjPrivateKeyAuthenticationException.InvalidKey()
        }

        val compactPayload = encodedPayload.copyOf(encodedSize)

        return try {
            Base64.getDecoder().decode(compactPayload)
        } catch (error: IllegalArgumentException) {
            throw SshjPrivateKeyAuthenticationException.InvalidKey(error)
        } finally {
            compactPayload.fill(0)
        }
    } finally {
        encodedPayload.fill(0)
    }
}

private fun ByteArray.isLineStart(
    index: Int,
): Boolean {
    return index == 0 ||
        this[index - 1] == '\n'.code.toByte()
}

private fun ByteArray.indexOfSequence(
    sequence: ByteArray,
    fromIndex: Int,
    limit: Int,
): Int {
    if (
        sequence.isEmpty() ||
        fromIndex < 0 ||
        limit > size ||
        fromIndex > limit
    ) {
        return -1
    }

    val lastStart = limit - sequence.size

    if (lastStart < fromIndex) {
        return -1
    }

    for (candidate in fromIndex..lastStart) {
        var matches = true

        for (offset in sequence.indices) {
            if (this[candidate + offset] != sequence[offset]) {
                matches = false
                break
            }
        }

        if (matches) {
            return candidate
        }
    }

    return -1
}

private fun Byte.isBase64Byte(): Boolean {
    val unsigned = toInt() and 0xff

    return unsigned in 'A'.code..'Z'.code ||
        unsigned in 'a'.code..'z'.code ||
        unsigned in '0'.code..'9'.code ||
        unsigned == '+'.code ||
        unsigned == '/'.code ||
        unsigned == '='.code
}

private fun Byte.isPemWhitespace(): Boolean {
    return this == ' '.code.toByte() ||
        this == '\t'.code.toByte() ||
        this == '\r'.code.toByte() ||
        this == '\n'.code.toByte()
}

private class OpenSshBinaryReader(
    private val bytes: ByteArray,
) {
    private var offset: Int = 0

    val hasRemaining: Boolean
        get() = offset < bytes.size

    fun requireMagic(
        expected: ByteArray,
    ) {
        requireAvailable(expected.size)

        for (index in expected.indices) {
            if (bytes[offset + index] != expected[index]) {
                throw SshjPrivateKeyAuthenticationException.InvalidKey()
            }
        }

        offset += expected.size
    }

    fun readAsciiString(): String {
        val value = readByteString()

        return try {
            String(value, StandardCharsets.US_ASCII)
        } finally {
            value.fill(0)
        }
    }

    fun readByteString(): ByteArray {
        val length = readUInt32()

        if (length > Int.MAX_VALUE.toLong()) {
            throw SshjPrivateKeyAuthenticationException.InvalidKey()
        }

        val intLength = length.toInt()
        requireAvailable(intLength)

        return bytes.copyOfRange(
            fromIndex = offset,
            toIndex = offset + intLength,
        ).also {
            offset += intLength
        }
    }

    fun readUInt32(): Long {
        requireAvailable(UINT32_SIZE_BYTES)

        val value =
            ((bytes[offset].toLong() and 0xffL) shl 24) or
                ((bytes[offset + 1].toLong() and 0xffL) shl 16) or
                ((bytes[offset + 2].toLong() and 0xffL) shl 8) or
                (bytes[offset + 3].toLong() and 0xffL)

        offset += UINT32_SIZE_BYTES
        return value
    }

    private fun requireAvailable(
        requiredSize: Int,
    ) {
        if (
            requiredSize < 0 ||
            offset < 0 ||
            offset > bytes.size ||
            requiredSize > bytes.size - offset
        ) {
            throw SshjPrivateKeyAuthenticationException.InvalidKey()
        }
    }
}

private const val UINT32_SIZE_BYTES: Int = 4
private const val OPENSSH_NONE: String = "none"
private const val OPENSSH_BCRYPT: String = "bcrypt"

private val OPENSSH_AUTH_MAGIC: ByteArray =
    "openssh-key-v1\u0000"
        .toByteArray(StandardCharsets.US_ASCII)

private val OPENSSH_PEM_BEGIN: ByteArray =
    ("-----BEGIN " + "OPENSSH PRIVATE KEY" + "-----")
        .toByteArray(StandardCharsets.US_ASCII)

private val OPENSSH_PEM_END: ByteArray =
    ("-----END " + "OPENSSH PRIVATE KEY" + "-----")
        .toByteArray(StandardCharsets.US_ASCII)
