package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.JvmArg

class JiraGcLog(
    private val jiraInstallation: String
) {
    companion object {
        const val FILE_NAME_PREFIX = "atlassian-jira-gc-"
    }

    fun path(
        suffix: String = "*"
    ): String = "$jiraInstallation/logs/$FILE_NAME_PREFIX$suffix"

    fun jvmArg(): JvmArg = JvmArg("-Xloggc:", path("%t.log"))
}