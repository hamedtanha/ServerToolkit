package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Encoding used before hashing an SSH host public key.
 *
 * OpenSshWire is the canonical SSH public-key representation used by OpenSSH fingerprint tools.
 * LegacyJavaPublicKey preserves compatibility with historical Server Toolkit SHA-256 trust rows
 * that hashed Java PublicKey.encoded bytes instead.
 */
enum class SshHostKeyFingerprintEncoding {
    OpenSshWire,
    LegacyJavaPublicKey,
}

/**
 * User-visible SSH host key fingerprint plus the encoding semantics required for exact trust
 * verification.
 *
 * The algorithm remains separate from the fingerprint value so presentation can render standard
 * fingerprints such as SHA256 values without parsing display text. Encoding is part of equality so
 * a historical Java-encoded SHA-256 value cannot be mistaken for a canonical OpenSSH SHA-256 value.
 */
data class SshHostKeyFingerprint(
    val algorithm: String,
    val value: String,
    val encoding: SshHostKeyFingerprintEncoding = SshHostKeyFingerprintEncoding.OpenSshWire,
) {
    init {
        require(algorithm.isNotBlank()) {
            "Host key fingerprint algorithm must not be blank."
        }
        require(value.isNotBlank()) {
            "Host key fingerprint value must not be blank."
        }
    }
}
