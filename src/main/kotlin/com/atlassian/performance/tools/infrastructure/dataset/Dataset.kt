package com.atlassian.performance.tools.infrastructure.dataset

import com.atlassian.performance.tools.infrastructure.database.Database
import com.atlassian.performance.tools.infrastructure.jira.JiraHomeSource

data class Dataset(
    val database: Database,
    val jiraHomeSource: JiraHomeSource,
    val label: String
)