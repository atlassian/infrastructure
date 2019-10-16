package com.atlassian.performance.tools.infrastructure.api.jira.hook

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.DefaultPostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.DefaultPostStartHook
import net.jcip.annotations.ThreadSafe

@ThreadSafe
class JiraNodeHooks private constructor() : PreInstallHooks() {

    companion object {
        fun default(): JiraNodeHooks = JiraNodeHooks()
            .apply { hook(DefaultPostStartHook()) }
            .apply { hook(DefaultPostInstallHook(JiraNodeConfig.Builder().build())) }

        fun empty(): JiraNodeHooks = JiraNodeHooks()
    }
}
