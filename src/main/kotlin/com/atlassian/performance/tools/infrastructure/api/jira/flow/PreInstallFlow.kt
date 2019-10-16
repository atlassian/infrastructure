package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.server.PreInstallHook

interface PreInstallFlow : PostInstallFlow {
    fun hook(
        hook: PreInstallHook
    ): PreInstallFlow
}
