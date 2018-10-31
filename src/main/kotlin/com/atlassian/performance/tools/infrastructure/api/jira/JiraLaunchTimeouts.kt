package com.atlassian.performance.tools.infrastructure.api.jira

import java.time.Duration

data class JiraLaunchTimeouts(
    val offlineTimeout: Duration,
    val initTimeout: Duration,
    val upgradeTimeout: Duration
)
