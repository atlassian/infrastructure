package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import java.net.URI

interface JiraInstance {
    val address: HttpNode
    val nodes: List<StartedJira>
}
