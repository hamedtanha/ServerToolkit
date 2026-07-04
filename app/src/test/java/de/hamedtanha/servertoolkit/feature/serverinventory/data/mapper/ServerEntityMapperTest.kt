package de.hamedtanha.servertoolkit.feature.serverinventory.data.mapper

import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerEntityMapperTest {

    @Test
    fun toEntity_mapsDomainModelToPersistenceModel() {
        val server = testServer()

        val entity = server.toEntity()

        assertEquals("server-1", entity.id)
        assertEquals("Production Alpha", entity.name)
        assertEquals("alpha.example.com", entity.host)
        assertEquals(2222, entity.sshPort)
        assertEquals("operator", entity.sshUsername)
        assertEquals("PRODUCTION", entity.environment)
        assertEquals("Linux", entity.category)
        assertEquals("production", entity.tags)
        assertEquals(true, entity.isFavorite)
        assertEquals("Primary production server", entity.description)
    }

    @Test
    fun toDomain_mapsPersistenceModelToDomainModel() {
        val entity = testServerEntity()

        val server = entity.toDomain()

        assertEquals("server-1", server.id)
        assertEquals("Production Alpha", server.name)
        assertEquals("alpha.example.com", server.host)
        assertEquals(2222, server.sshPort)
        assertEquals("operator", server.sshUsername)
        assertEquals(ServerEnvironment.PRODUCTION, server.environment)
        assertEquals("Linux", server.category)
        assertEquals(listOf("production"), server.tags)
        assertEquals(true, server.isFavorite)
        assertEquals("Primary production server", server.description)
    }

    @Test
    fun toDomain_whenEnvironmentIsUnknownValue_returnsUnknownEnvironment() {
        val entity = testServerEntity(environment = "LEGACY_ENVIRONMENT")

        val server = entity.toDomain()

        assertEquals(ServerEnvironment.UNKNOWN, server.environment)
    }

    @Test
    fun toDomain_whenTagsAreBlank_returnsEmptyTagList() {
        val entity = testServerEntity(tags = "")

        val server = entity.toDomain()

        assertEquals(emptyList<String>(), server.tags)
    }

    private fun testServer(): Server {
        return Server(
            id = "server-1",
            name = "Production Alpha",
            host = "alpha.example.com",
            sshPort = 2222,
            sshUsername = "operator",
            environment = ServerEnvironment.PRODUCTION,
            category = "Linux",
            tags = listOf("production"),
            isFavorite = true,
            description = "Primary production server",
        )
    }

    private fun testServerEntity(
        environment: String = "PRODUCTION",
        tags: String = "production",
    ): ServerEntity {
        return ServerEntity(
            id = "server-1",
            name = "Production Alpha",
            host = "alpha.example.com",
            sshPort = 2222,
            sshUsername = "operator",
            environment = environment,
            category = "Linux",
            tags = tags,
            isFavorite = true,
            description = "Primary production server",
        )
    }
}
