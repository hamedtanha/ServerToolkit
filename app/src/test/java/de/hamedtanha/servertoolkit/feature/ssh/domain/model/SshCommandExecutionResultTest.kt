package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SshCommandExecutionResultTest {

    @Test
    fun `redacts command output from string representation`() {
        val output = SshCommandExecutionOutput(
            stdout = "token=secret",
            stderr = "password failed",
            exitStatus = 0,
        )

        assertEquals(
            "SshCommandExecutionOutput(stdout=REDACTED, stderr=REDACTED, exitStatus=0)",
            output.toString(),
        )
    }

    @Test
    fun `keeps nullable exit status in string representation`() {
        val output = SshCommandExecutionOutput(
            stdout = "token=secret",
            stderr = "password failed",
            exitStatus = null,
        )

        assertEquals(
            "SshCommandExecutionOutput(stdout=REDACTED, stderr=REDACTED, exitStatus=null)",
            output.toString(),
        )
    }
}
