package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira

class StartedJira(
    val installed: InstalledJira,
    val pid: Int
)
