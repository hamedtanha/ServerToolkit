package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SshAuthenticationMethodTest {

    @Test
    fun `defines supported authentication input methods`() {
        assertEquals(
            listOf(
                SshAuthenticationMethod.PASSWORD,
                SshAuthenticationMethod.PRIVATE_KEY,
            ),
            SshAuthenticationMethod.entries,
        )
    }
}
