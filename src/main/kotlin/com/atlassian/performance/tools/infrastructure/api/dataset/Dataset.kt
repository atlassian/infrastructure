package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource

class Dataset(
    val database: Database,
    val jiraHomeSource: JiraHomeSource,
    val label: String
) {

    override fun toString(): String {
        return "Dataset(database=$database, jiraHomeSource=$jiraHomeSource, label='$label')"
    }
}