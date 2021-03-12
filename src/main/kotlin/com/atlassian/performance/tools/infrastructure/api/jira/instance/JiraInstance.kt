package com.atlassian.performance.tools.infrastructure.api.jira.instance

import java.net.URI

interface JiraInstance {
    val address: URI
}