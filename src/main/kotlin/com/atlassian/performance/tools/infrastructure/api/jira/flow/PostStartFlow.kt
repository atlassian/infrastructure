package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PostStartHook

interface PostStartFlow : Reports {
    fun hook(
        hook: PostStartHook
    ): PostStartFlow
}
