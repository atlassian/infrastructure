package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PreInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PreStartHook
import net.jcip.annotations.ThreadSafe
import java.util.concurrent.CopyOnWriteArrayList

@ThreadSafe
class JiraNodeFlow {

    val preInstallHooks: MutableList<PreInstallHook> = CopyOnWriteArrayList<PreInstallHook>()
    val postInstallHooks: MutableList<PostInstallHook> = CopyOnWriteArrayList<PostInstallHook>()
    val preStartHooks: MutableList<PreStartHook> = CopyOnWriteArrayList<PreStartHook>()
    val postStartHooks: MutableList<PostStartHook> = CopyOnWriteArrayList<PostStartHook>()
    val reports: MutableList<Report> = CopyOnWriteArrayList<Report>()
}
