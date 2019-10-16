package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.DefaultPostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.DefaultPostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.jcip.annotations.ThreadSafe
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@ThreadSafe
class JiraNodeFlow private constructor() : PreInstallFlow {
    private val preInstallHooks: Queue<PreInstallHook> = ConcurrentLinkedQueue()
    private val postInstallHooks: Queue<PostInstallHook> = ConcurrentLinkedQueue()
    private val preStartHooks: Queue<PreStartHook> = ConcurrentLinkedQueue()
    private val postStartHooks: Queue<PostStartHook> = ConcurrentLinkedQueue()
    private val reports: Queue<Report> = ConcurrentLinkedQueue()

    override fun hook(
        hook: PreInstallHook
    ) = apply {
        preInstallHooks.add(hook)
    }

    internal fun runPreInstallHooks(
        ssh: SshConnection,
        server: TcpServer
    ): JiraNodeFlow = apply {
        while (true) {
            preInstallHooks
                .poll()
                ?.run(ssh, server, this)
                ?: break
        }
    }

    override fun hook(
        hook: PostInstallHook
    ): PreInstallFlow = apply {
        postInstallHooks.add(hook)
    }

    internal fun runPostInstallHooks(
        ssh: SshConnection,
        jira: InstalledJira
    ): JiraNodeFlow = apply {
        while (true) {
            postInstallHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }

    override fun hook(
        hook: PreStartHook
    ): JiraNodeFlow = apply {
        preStartHooks.add(hook)
    }

    internal fun runPreStartHooks(
        ssh: SshConnection,
        jira: InstalledJira
    ): JiraNodeFlow = apply {
        while (true) {
            preStartHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }

    override fun hook(
        hook: PostStartHook
    ): JiraNodeFlow = apply {
        postStartHooks.add(hook)
    }

    internal fun runPostStartHooks(
        ssh: SshConnection,
        jira: StartedJira
    ): JiraNodeFlow = apply {
        while (true) {
            postStartHooks
                .poll()
                ?.run(ssh, jira, this)
                ?: break
        }
    }

    override fun addReport(
        report: Report
    ) = apply {
        reports.add(report)
    }

    internal fun allReports(): Iterable<Report> {
        return reports.toList()
    }

    fun copy(): JiraNodeFlow = JiraNodeFlow()
        .also { it.preInstallHooks += this.preInstallHooks }
        .also { it.postInstallHooks += this.postInstallHooks }
        .also { it.preStartHooks += this.preStartHooks }
        .also { it.postStartHooks += this.postStartHooks }
        .also { it.reports += this.reports }

    companion object {
        fun default(): JiraNodeFlow = JiraNodeFlow()
            .apply { hook(DefaultPostInstallHook(JiraNodeConfig.Builder().build())) }
            .apply { hook(DefaultPostStartHook()) }

        fun empty(): JiraNodeFlow = JiraNodeFlow()
    }
}
