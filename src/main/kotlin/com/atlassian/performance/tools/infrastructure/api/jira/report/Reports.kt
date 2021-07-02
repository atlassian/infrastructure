package com.atlassian.performance.tools.infrastructure.api.jira.report

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpNode
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath
import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.io.api.resolveSafely
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class Reports private constructor( // TODO turn into SPI to allow AWS CLI transport (S3)
    private val hostReports: Queue<HostReport>
) {
    constructor() : this(ConcurrentLinkedQueue())

    fun add(report: Report, started: StartedJira) {
        add(report, started.installed)
    }

    fun add(report: Report, installed: InstalledJira) {
        add(report, installed.http)
    }

    fun add(report: Report, http: HttpNode) {
        hostReports.add(HostReport(http.tcp, report))
    }

    fun add(report: Report, tcp: TcpNode) {
        hostReports.add(HostReport(tcp, report))
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
        val host: TcpNode,
        val report: Report
    )
}
