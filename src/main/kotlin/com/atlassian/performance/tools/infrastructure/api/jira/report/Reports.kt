package com.atlassian.performance.tools.infrastructure.api.jira.report

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.io.api.resolveSafely
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class Reports private constructor(
    private val hostReports: Queue<HostReport>
) {
    constructor() : this(ConcurrentLinkedQueue())

    fun add(
        report: Report,
        host: TcpHost
    ) {
        hostReports.add(HostReport(host, report))
    }

    fun downloadTo(
        localDirectory: Path
    ): File {
        localDirectory.ensureDirectory()
        hostReports.groupBy { it.host }.map { (host, reports) ->
            host.ssh.newConnection().use { ssh ->
                val remoteBase = RemotePath(ssh.getHost(), ssh.execute("pwd").output.trim())
                reports
                    .flatMap { report ->
                        report.report.locate(ssh).map { path -> RemotePath(host.ssh.host, path) }
                    }
                    .forEach { remotePath ->
                        localDirectory
                            .resolveSafely(host.name)
                            .resolve(remoteBase.toLocalRelativePath())
                            .resolve(remotePath.toLocalRelativePath())
                            .normalize()
                            .let { remotePath.download(it) }
                    }
            }
        }
        return localDirectory.toFile()
    }

    private fun RemotePath.toLocalRelativePath(): Path = Paths.get(path.trimStart('/'))

    fun copy(): Reports {
        return Reports(ConcurrentLinkedQueue(hostReports))
    }

    private class HostReport(
        val host: TcpHost,
        val report: Report
    )
}
