package de.hamedtanha.servertoolkit.core.connection.domain.model

sealed interface ConnectionTargetResolution {

    data class Resolved(
        val target: RemoteConnectionTarget,
    ) : ConnectionTargetResolution

    data object NotFound : ConnectionTargetResolution

    data class Invalid(
        val reason: ConnectionTargetInvalidReason,
    ) : ConnectionTargetResolution
}
