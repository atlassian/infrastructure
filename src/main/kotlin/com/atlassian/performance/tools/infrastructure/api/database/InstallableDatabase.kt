package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.InstalledJiraHook

interface InstallableDatabase : Database {

    fun installInJira(databaseIp: String): InstalledJiraHook
}
