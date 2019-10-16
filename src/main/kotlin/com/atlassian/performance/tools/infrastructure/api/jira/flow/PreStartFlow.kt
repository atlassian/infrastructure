package com.atlassian.performance.tools.infrastructure.api.jira.flow


interface PreStartFlow : PostStartFlow {
    fun hook(
        hook: PreStartHook
    ): PreStartFlow
}
