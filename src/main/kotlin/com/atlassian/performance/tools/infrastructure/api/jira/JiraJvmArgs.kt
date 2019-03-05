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
    ): List<JvmArg> = java8Arguments() + debug.getJvmOption() + jmx.getJvmArgs(jiraIp) + extra

    fun arguments(
        debug: JvmDebug,
        jmx: RemoteJmx,
        jiraIp: String,
        jdkVersion: Int
    ): List<JvmArg> {
        val jvmArg = if (jdkVersion > 8) java9Arguments() else java8Arguments()
        return jvmArg + debug.getJvmOption() + jmx.getJvmArgs(jiraIp) + extra
    }

    private fun java8Arguments(): List<JvmArg> {
        return commonArguments() + listOf(
            JvmArg("-XX:+PrintGCDetails"),
            JvmArg("-XX:+PrintGCDateStamps"),
            JvmArg("-XX:+PrintGCTimeStamps"),
            JvmArg("-XX:+PrintGCCause"),
            JvmArg("-XX:+PrintTenuringDistribution"),
            JvmArg("-XX:+PrintGCApplicationStoppedTime"),
            JvmArg("-XX:+UseGCLogFileRotation"),
            JvmArg("-XX:NumberOfGCLogFiles=", "5"),
            JvmArg("-XX:GCLogFileSize=", "20M")
        )
    }

    private fun commonArguments(): List<JvmArg> {
        return listOf(
            JvmArg("-Datlassian.darkfeature.jira.onboarding.feature.disabled=", "true"),
            JvmArg("-Djira.startup.warnings.disable=", "true")
        )
    }

    private fun java9Arguments(): List<JvmArg> {
        return commonArguments() + listOf(
            JvmArg("-Xlog:gc*:file=atlassian-jira-gc-%t.log:time,uptime:filecount=5,filesize=20M")
        )
    }

}