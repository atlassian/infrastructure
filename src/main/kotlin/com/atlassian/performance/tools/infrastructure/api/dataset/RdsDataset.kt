package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.api.database.RdsDatabase
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource

/**
 * AWS RDS based dataset.
 */
class RdsDataset(
    val database: RdsDatabase,
    val jiraHomeSource: JiraHomeSource,
    val label: String
) {

    override fun toString(): String {
        return "Dataset(database=$database, jiraHomeSource=$jiraHomeSource, label='$label')"
    }

    /**
     * @since 4.13.0
     */
    class Builder(
        original: RdsDataset
    ) {
        private var database: RdsDatabase = original.database
        private var jiraHomeSource: JiraHomeSource = original.jiraHomeSource
        private var label: String = original.label

        fun database(database: RdsDatabase) = apply { this.database = database }
        fun jiraHomeSource(jiraHomeSource: JiraHomeSource) = apply { this.jiraHomeSource = jiraHomeSource }
        fun label(label: String) = apply { this.label = label }

        fun build(): RdsDataset = RdsDataset(
            database = database,
            jiraHomeSource = jiraHomeSource,
            label = label
        )
    }
}
