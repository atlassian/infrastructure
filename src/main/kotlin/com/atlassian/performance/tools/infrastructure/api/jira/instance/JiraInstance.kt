package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import java.net.URI

interface JiraInstance {
    val address: URI
    val nodes: List<StartedJira>
}