package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx
import com.atlassian.performance.tools.infrastructure.api.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder

class JiraNodeConfig(
    val name: String,
    val debug: JvmDebug,
    val remoteJmx: RemoteJmx,
    val jvmArgs: JiraJvmArgs,
    val splunkForwarder: SplunkForwarder,
    val launchTimeouts: JiraLaunchTimeouts
) {
    constructor(
        name: String,
        jvmArgs: JiraJvmArgs,
        launchTimeouts: JiraLaunchTimeouts
    ) : this(
        name = name,
        debug = DisabledJvmDebug(),
        remoteJmx = DisabledRemoteJmx(),
        jvmArgs = jvmArgs,
        splunkForwarder = DisabledSplunkForwarder(),
        launchTimeouts = launchTimeouts
    )

    fun clone(
        times: Int
    ): List<JiraNodeConfig> = (1..times).map {
        JiraNodeConfig(
            name = "$name-$it",
            debug = debug,
            remoteJmx = remoteJmx,
            jvmArgs = jvmArgs,
            splunkForwarder= splunkForwarder,
            launchTimeouts = launchTimeouts
        )
    }

    override fun toString(): String {
        return "JiraNodeConfig(name='$name', debug=$debug, remoteJmx=$remoteJmx, jvmArgs=$jvmArgs, splunkForwarder=$splunkForwarder, launchTimeouts=$launchTimeouts)"
    }
}