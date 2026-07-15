package de.hamedtanha.servertoolkit.feature.savedcommands.domain.model

/**
 * Project-owned representation of reusable operational command text.
 *
 * Saved commands are not credentials and are never executed by this model. The command value is
 * preserved exactly so that later presentation integration can populate the existing SSH command
 * input without parsing, rewriting, or automatic execution.
 */
data class SavedCommand(
    val id: String,
    val name: String,
    val command: String,
    val createdAtEpochMillis: Long,
) {
    init {
        require(id.isNotBlank()) {
            "Saved command id must not be blank."
        }
        require(name.isNotBlank()) {
            "Saved command name must not be blank."
        }
        require(name == name.trim()) {
            "Saved command name must not contain leading or trailing whitespace."
        }
        require(name.length <= MAX_NAME_LENGTH) {
            "Saved command name must not exceed $MAX_NAME_LENGTH characters."
        }
        require(command.isNotBlank()) {
            "Saved command text must not be blank."
        }
        require(command.length <= MAX_COMMAND_LENGTH) {
            "Saved command text must not exceed $MAX_COMMAND_LENGTH characters."
        }
        require(createdAtEpochMillis > 0) {
            "Saved command creation time must be positive."
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 100
        const val MAX_COMMAND_LENGTH = 16_384
    }
}
