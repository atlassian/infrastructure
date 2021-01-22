package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.DefaultPostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.DefaultPostStartHook
import net.jcip.annotations.ThreadSafe

@ThreadSafe
class JiraNodeHooks {

    val preInstall = PreInstallHooks()
    val postInstall = preInstall.postInstall
    val preStart = postInstall.preStart
    val postStart = preStart.postStart
    val reports = postStart.reports

    companion object {
        fun default(): JiraNodeHooks = JiraNodeHooks()
            .apply { postStart.insert(DefaultPostStartHook()) }
            .apply { postInstall.insert(DefaultPostInstallHook(JiraNodeConfig.Builder().build())) }

        fun empty(): JiraNodeHooks = JiraNodeHooks()
    }
}
