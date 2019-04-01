package com.atlassian.performance.tools.infrastructure.api.database

import com.atlassian.performance.tools.infrastructure.api.jira.flow.install.Install

interface InstallableDatabase : Database {

    fun installInJira(databaseIp: String): Install
}
