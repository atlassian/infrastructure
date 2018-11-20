package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx
import com.atlassian.performance.tools.infrastructure.api.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import java.time.Duration

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

    @Deprecated(message = "Use the other constructor")
    constructor(
        name: String = "jira-node",
        debug: JvmDebug = DisabledJvmDebug(),
        remoteJmx: RemoteJmx = DisabledRemoteJmx(),
        jvmArgs: JiraJvmArgs = JiraJvmArgs(),
        splunkForwarder: SplunkForwarder = DisabledSplunkForwarder()
    ) : this(
        name = name,
        debug = debug,
        remoteJmx = remoteJmx,
        jvmArgs = jvmArgs,
        splunkForwarder = splunkForwarder,
        launchTimeouts = JiraLaunchTimeouts(
            offlineTimeout = Duration.ofMinutes(8),
            initTimeout = Duration.ofMinutes(4),
            upgradeTimeout = Duration.ofMinutes(8),
            unresponsivenessTimeout = Duration.ofMinutes(4)
        )
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