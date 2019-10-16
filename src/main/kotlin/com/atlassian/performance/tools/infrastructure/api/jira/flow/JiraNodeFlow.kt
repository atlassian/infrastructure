package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig
import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.DefaultPostInstallHook
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.DefaultPostStartHook
import net.jcip.annotations.ThreadSafe

@ThreadSafe
class JiraNodeFlow private constructor() : PreInstallFlow() {

    companion object {
        fun default(): JiraNodeFlow = JiraNodeFlow()
            .apply { hook(DefaultPostStartHook()) }
            .apply { hook(DefaultPostInstallHook(JiraNodeConfig.Builder().build())) }

        fun empty(): JiraNodeFlow = JiraNodeFlow()
    }
}
