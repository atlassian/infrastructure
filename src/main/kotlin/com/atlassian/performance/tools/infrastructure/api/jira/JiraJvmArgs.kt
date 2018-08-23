package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.JvmArg
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx

class JiraJvmArgs(
    val xms: String = "384m",
    val xmx: String = "8g",
    private val extra: List<JvmArg> = emptyList()
) {
    fun arguments(
        debug: JvmDebug,
        jmx: RemoteJmx,
        jiraIp: String
    ): List<JvmArg> = listOf(
        JvmArg("-Datlassian.darkfeature.jira.onboarding.feature.disabled=", "true"),
        JvmArg("-Djira.startup.warnings.disable=", "true"),
        JvmArg("-XX:+PrintGCDetails"),
        JvmArg("-XX:+PrintGCDateStamps"),
        JvmArg("-XX:+PrintGCTimeStamps"),
        JvmArg("-XX:+PrintGCCause"),
        JvmArg("-XX:+PrintTenuringDistribution"),
        JvmArg("-XX:+PrintGCApplicationStoppedTime"),
        JvmArg("-XX:+UseGCLogFileRotation"),
        JvmArg("-XX:NumberOfGCLogFiles=", "5"),
        JvmArg("-XX:GCLogFileSize=", "20M")
    ) + debug.getJvmOption() + jmx.getJvmArgs(jiraIp) + extra
}