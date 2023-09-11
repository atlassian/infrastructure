package com.atlassian.performance.tools.infrastructure.hookapi.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira

interface JiraInstance {
    val address: HttpNode
    val nodes: List<StartedJira>
}
