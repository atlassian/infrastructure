package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook

interface PostInstallFlow : PreStartFlow {
    fun hook(
        hook: PostInstallHook
    ): PostInstallFlow
}
