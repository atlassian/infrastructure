package com.atlassian.performance.tools.infrastructure.app

import java.io.File

class Apps(
    private val apps: List<AppSource>
) {
    fun listFiles(): List<File> {
        val pluginsDirectory = createTempDir("jira-plugins")
        return apps.flatMap { it.acquireFiles(pluginsDirectory) }
    }
}