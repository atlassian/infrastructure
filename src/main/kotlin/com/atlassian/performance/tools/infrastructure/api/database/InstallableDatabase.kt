package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.PostInstallHook

interface InstallableDatabase : Database {

    fun installInJira(databaseIp: String): PostInstallHook
}
