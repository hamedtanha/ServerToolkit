package de.hamedtanha.servertoolkit.feature.ssh.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "ssh_trusted_host_keys",
    primaryKeys = ["server_id", "host", "port"],
)
data class SshTrustedHostKeyEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    val host: String,
    val port: Int,
    @ColumnInfo(name = "fingerprint_algorithm")
    val fingerprintAlgorithm: String,
    @ColumnInfo(name = "fingerprint_value")
    val fingerprintValue: String,
)
