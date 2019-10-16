package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@ThreadSafe
class JiraNodeFlow : PreInstallFlow {
    private val preInstallHooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()
    private val postInstallHooks: Queue<PostInstallHook> = ConcurrentLinkedQueue()
    private val preStartHooks: Queue<PreStartHook> = ConcurrentLinkedQueue()
    private val postStartHooks: Queue<PostStartHook> = ConcurrentLinkedQueue()
    private val reports: Queue<Report> = ConcurrentLinkedQueue()

    override fun hook(
        hook: PreInstallHook
    ) {
        preInstallHooks.add(hook)
    }

    internal fun runPreInstallHooks(
        ssh: SshConnection,
        server: TcpServer
    ) {
        while (true) {
            preInstallHooks
                .poll()
                ?.run(ssh, server, this)
                ?: break
        }
    }

    override fun hook(
        hook: PostInstallHook
    ) {
        postInstallHooks.add(hook)
    }

    internal fun listPostInstallHooks(): Iterable<PostInstallHook> = postInstallHooks

    override fun hook(
        hook: PreStartHook
    ) {
        preStartHooks.add(hook)
    }

    internal fun listPreStartHooks(): Iterable<PreStartHook> = preStartHooks

    override fun hook(
        hook: PostStartHook
    ) {
        postStartHooks.add(hook)
    }

    internal fun listPostStartHooks(): Iterable<PostStartHook> = postStartHooks

    override fun addReport(report: Report) {
        reports.add(report)
    }

    internal fun allReports(): Iterable<Report> {
        return reports.toList()
    }
}
