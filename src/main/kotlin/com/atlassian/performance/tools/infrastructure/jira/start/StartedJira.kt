package com.atlassian.performance.tools.infrastructure.jira.start

import com.atlassian.performance.tools.infrastructure.jira.install.InstalledJira

class StartedJira(
    val installed: InstalledJira,
    val pid: Int
)
