package com.atlassian.performance.tools.infrastructure.jira.nodes

import com.atlassian.performance.tools.infrastructure.jira.JiraJvmArgs
import com.atlassian.performance.tools.infrastructure.jira.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.jira.splunk.SplunkForwarder
import com.atlassian.performance.tools.infrastructure.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.jvm.jmx.RemoteJmx

data class JiraNodeConfig(
    val name: String = "jira-node",
    val debug: JvmDebug = DisabledJvmDebug(),
    val remoteJmx: RemoteJmx = DisabledRemoteJmx(),
    val jvmArgs: JiraJvmArgs = JiraJvmArgs(),
    val splunkForwarder: SplunkForwarder = DisabledSplunkForwarder()
) {
    fun clone(
        times: Int
    ): List<JiraNodeConfig> = (1..times).map { copy(name = "$name-$it") }
}