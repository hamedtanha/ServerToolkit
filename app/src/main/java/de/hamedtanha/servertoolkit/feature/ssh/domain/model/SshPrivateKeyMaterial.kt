package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Short-lived project-owned private-key material.
 *
 * The backing buffer is available only to internal implementation code and is cleared on a
 * best-effort basis when its owning source finishes the consumption block.
 */
class SshPrivateKeyMaterial internal constructor(
    private val bytes: ByteArray,
    validSize: Int,
) {

    private var validSize: Int = validSize

    internal var isCleared: Boolean = false
        private set

    init {
        require(validSize in 1..bytes.size) {
            "Private-key material must contain at least one valid byte."
        }
    }

    val size: Int
        get() = if (isCleared) 0 else validSize

    internal fun <T> useBytes(
        block: (bytes: ByteArray, size: Int) -> T,
    ): T {
        check(!isCleared) {
            "Private-key material has already been cleared."
        }

        return block(bytes, validSize)
    }

    internal fun clear() {
        if (isCleared) {
            return
        }

        bytes.fill(0)
        validSize = 0
        isCleared = true
    }

    override fun toString(): String {
        return "SshPrivateKeyMaterial(REDACTED)"
    }
}
