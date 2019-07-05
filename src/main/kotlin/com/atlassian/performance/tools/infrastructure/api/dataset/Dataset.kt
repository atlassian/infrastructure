package com.atlassian.performance.tools.infrastructure.api.dataset

import com.atlassian.performance.tools.infrastructure.api.database.Database
import com.atlassian.performance.tools.infrastructure.api.jira.JiraHomeSource

/**
 * SSH-based dataset.
 */
class Dataset(
    val database: Database,
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
        original: Dataset
    ) {
        private var database: Database = original.database
        private var jiraHomeSource: JiraHomeSource = original.jiraHomeSource
        private var label: String = original.label

        fun database(database: Database) = apply { this.database = database }
        fun jiraHomeSource(jiraHomeSource: JiraHomeSource) = apply { this.jiraHomeSource = jiraHomeSource }
        fun label(label: String) = apply { this.label = label }

        fun build(): Dataset = Dataset(
            database = database,
            jiraHomeSource = jiraHomeSource,
            label = label
        )
    }
}
