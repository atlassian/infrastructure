package com.atlassian.performance.tools.infrastructure.api.jira.flow.server

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJira

class StartedJira(
    val installed: InstalledJira,
    val pid: Int
)
