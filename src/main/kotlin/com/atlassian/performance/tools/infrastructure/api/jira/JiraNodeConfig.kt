package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx
import com.atlassian.performance.tools.infrastructure.api.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder

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