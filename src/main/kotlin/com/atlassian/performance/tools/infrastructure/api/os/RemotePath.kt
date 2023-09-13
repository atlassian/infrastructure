package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.infrastructure.api.dataset.FileArchiver
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshHost
import java.io.File
import java.nio.file.Path
import java.time.Duration
import javax.json.Json
import javax.json.JsonObject

/**
 * @since 4.18.0
 */
class RemotePath(
    val host: SshHost,
    val path: String
) {

    constructor(json: JsonObject) : this(
        host = SshHost(json.getJsonObject("host")),
        path = json.getString("path")
    )

    fun toJson(): JsonObject = Json.createObjectBuilder()
        .add("host", host.toJson())
        .add("path", path)
        .build()

    fun resolve(other: String): RemotePath = RemotePath(host, "$path/$other")

    fun move(remoteDestination: String, timeout: Duration): RemotePath {
        if (path != remoteDestination) {
            Ssh(host, connectivityPatience = 4).newConnection().use { ssh ->
                ssh.execute("sudo mv $path $remoteDestination", timeout)
            }
        }
        return RemotePath(host, remoteDestination)
    }

    fun archive(timeout: Duration): RemotePath {
        val remoteZip = Ssh(host, connectivityPatience = 4).newConnection().use { ssh ->
            FileArchiver().zip(ssh, path, timeout)
        }
        return RemotePath(host, remoteZip)
    }

    fun download(
        localDestination: Path
    ): File {
        Ssh(host, connectivityPatience = 4).newConnection().use { ssh ->
            ssh.download(path, localDestination)
        }
        return localDestination.toFile()
    }

    fun upload(
        localSource: File
    ): RemotePath {
        Ssh(host, connectivityPatience = 4).newConnection().use { ssh ->
            ssh.upload(localSource, path)
        }
        return this
    }

    override fun toString(): String {
        return "RemotePath(host=$host, path='$path')"
    }
}
