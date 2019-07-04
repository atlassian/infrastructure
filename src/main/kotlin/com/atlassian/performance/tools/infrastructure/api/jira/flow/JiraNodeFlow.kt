package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.StartedJiraHook
import net.jcip.annotations.ThreadSafe
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@ThreadSafe
class JiraNodeFlow {

    private val preInstallHooks: Queue<TcpServerHook> = ConcurrentLinkedQueue()
    private val postInstallHooks: Queue<InstalledJiraHook> = ConcurrentLinkedQueue()
    private val preStartHooks: Queue<InstalledJiraHook> = ConcurrentLinkedQueue()
    private val postStartHooks: Queue<StartedJiraHook> = ConcurrentLinkedQueue()
    val reports: Queue<Report> = ConcurrentLinkedQueue()

    fun hookPreInstall(
        hook: TcpServerHook
    ) {
        preInstallHooks.add(hook)
    }

    internal fun listPreInstallHooks(): Iterable<TcpServerHook> = preInstallHooks

    fun hookPostInstall(
        hook: InstalledJiraHook
    ) {
        postInstallHooks.add(hook)
    }

    internal fun listPostInstallHooks(): Iterable<InstalledJiraHook> = postInstallHooks

    fun hookPreStart(
        hook: InstalledJiraHook
    ) {
        preStartHooks.add(hook)
    }

    internal fun listPreStartHooks(): Iterable<InstalledJiraHook> = preStartHooks

    fun hookPostStart(
        hook: StartedJiraHook
    ) {
        postStartHooks.add(hook)
    }

    internal fun listPostStartHooks(): Iterable<StartedJiraHook> = postStartHooks
}
