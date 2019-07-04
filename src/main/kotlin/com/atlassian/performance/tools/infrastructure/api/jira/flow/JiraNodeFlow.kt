package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.TcpServerHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.StartedJiraHook
import net.jcip.annotations.ThreadSafe
import java.util.concurrent.CopyOnWriteArrayList

@ThreadSafe
class JiraNodeFlow {

    private val tcpServerHooks: MutableList<TcpServerHook> = CopyOnWriteArrayList()
    private val installedJiraHooks: MutableList<InstalledJiraHook> = CopyOnWriteArrayList()
    private val preStartHooks: MutableList<InstalledJiraHook> = CopyOnWriteArrayList()
    private val postStartHooks: MutableList<StartedJiraHook> = CopyOnWriteArrayList()
    val reports: MutableList<Report> = CopyOnWriteArrayList()

    fun hookPreInstall(
        hook: TcpServerHook
    ) {
        tcpServerHooks.add(hook)
    }

    internal fun listPreInstallHooks(): Iterable<TcpServerHook> = tcpServerHooks

    fun hookPostInstall(
        hook: InstalledJiraHook
    ) {
        installedJiraHooks.add(hook)
    }

    internal fun listPostInstallHooks(): Iterable<InstalledJiraHook> = installedJiraHooks

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
