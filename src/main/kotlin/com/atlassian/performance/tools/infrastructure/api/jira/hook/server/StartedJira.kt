package com.atlassian.performance.tools.infrastructure.api.jira.hook.server

import com.atlassian.performance.tools.infrastructure.api.jira.hook.install.InstalledJira

class StartedJira(
    val installed: InstalledJira,
    val pid: Int
)
