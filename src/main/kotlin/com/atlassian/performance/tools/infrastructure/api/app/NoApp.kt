package com.atlassian.performance.tools.infrastructure.api.app

import java.io.File

class NoApp @JvmOverloads constructor(
    private val label: String = "plain-jira"
) : AppSource {
    override fun getLabel(): String = label

    override fun acquireFiles(directory: File): List<File> = emptyList()
}