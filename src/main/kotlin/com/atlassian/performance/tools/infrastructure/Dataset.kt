package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.jira.home.JiraHomeSource

data class Dataset(
    val database: Database,
    val jiraHomeSource: JiraHomeSource,
    val label: String
)