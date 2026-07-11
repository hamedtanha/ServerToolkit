package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeyMaterial
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult

/**
 * Project-owned source for private-key material that may be consumed by one operation only.
 *
 * Implementations must keep Android document APIs, streams, and third-party SSH types behind the
 * data-layer boundary. The supplied block is the complete lifetime of the exposed key material.
 */
interface SshPrivateKeySource {

    suspend fun <T> consume(
        block: suspend SshPrivateKeyMaterial.() -> T,
    ): SshPrivateKeySourceResult<T>

    /**
     * Invalidates an available source.
     *
     * @return `true` only when this call performed the available-to-invalidated transition.
     */
    fun invalidate(): Boolean
}
