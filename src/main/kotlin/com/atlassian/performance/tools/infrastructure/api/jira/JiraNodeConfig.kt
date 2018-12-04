package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx
import com.atlassian.performance.tools.infrastructure.api.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import java.net.URI

class JiraNodeConfig @Deprecated(message = "Use JiraNodeConfig.Builder instead.") constructor(
    val name: String,
    val debug: JvmDebug,
    val remoteJmx: RemoteJmx,
    val jvmArgs: JiraJvmArgs,
    val collectdConfigs: List<URI>,
    val splunkForwarder: SplunkForwarder,
    val launchTimeouts: JiraLaunchTimeouts
) {

    @Suppress("DEPRECATION")
    @Deprecated(message = "Use JiraNodeConfig.Builder instead.")
    constructor(
        name: String,
        jvmArgs: JiraJvmArgs,
        launchTimeouts: JiraLaunchTimeouts
    ) : this(
        name = name,
        debug = DisabledJvmDebug(),
        remoteJmx = DisabledRemoteJmx(),
        jvmArgs = jvmArgs,
        collectdConfigs = DEFAULT_COLLECTD_CONFIGS,
        splunkForwarder = DisabledSplunkForwarder(),
        launchTimeouts = launchTimeouts
    )

    @Deprecated(message = "Use JiraNodeConfig.Builder instead.",
        replaceWith = ReplaceWith(
            "(1..times).map { Builder(jiraNodeConfig).name(\"\$name-\$it\").build() }",
            "com.atlassian.performance.tools.infrastructure.api.jira.JiraNodeConfig.Builder"
        )
    )
    fun clone(
        times: Int
    ): List<JiraNodeConfig> = (1..times).map {
        Builder(this)
            .name("$name-$it")
            .build()
    }

    override fun toString(): String {
        return "JiraNodeConfig(name='$name', debug=$debug, remoteJmx=$remoteJmx, jvmArgs=$jvmArgs, " +
            "collectdConfigs=$collectdConfigs, splunkForwarder=$splunkForwarder, launchTimeouts=$launchTimeouts)"
    }

    class Builder() {
        private var name: String = "jira-node"
        private var debug: JvmDebug = DisabledJvmDebug()
        private var remoteJmx: RemoteJmx = DisabledRemoteJmx()
        private var jvmArgs: JiraJvmArgs = JiraJvmArgs()
        private var collectdConfigs: List<URI> = DEFAULT_COLLECTD_CONFIGS
        private var splunkForwarder: SplunkForwarder = DisabledSplunkForwarder()
        private var launchTimeouts: JiraLaunchTimeouts = JiraLaunchTimeouts.Builder().build()

        constructor(
            jiraNodeConfig: JiraNodeConfig
        ) : this() {
            name = jiraNodeConfig.name
            debug = jiraNodeConfig.debug
            remoteJmx = jiraNodeConfig.remoteJmx
            jvmArgs = jiraNodeConfig.jvmArgs
            splunkForwarder = jiraNodeConfig.splunkForwarder
            launchTimeouts = jiraNodeConfig.launchTimeouts
        }

        fun name(name: String) = apply { this.name = name }
        fun debug(debug: JvmDebug) = apply { this.debug = debug }
        fun remoteJmx(remoteJmx: RemoteJmx) = apply { this.remoteJmx = remoteJmx }
        fun jvmArgs(jvmArgs: JiraJvmArgs) = apply { this.jvmArgs = jvmArgs }
        fun splunkForwarder(splunkForwarder: SplunkForwarder) = apply { this.splunkForwarder = splunkForwarder }
        fun launchTimeouts(launchTimeouts: JiraLaunchTimeouts) = apply { this.launchTimeouts = launchTimeouts }
        fun collectdConfig(collectdConfigs: List<URI>) = apply { this.collectdConfigs = collectdConfigs }

        @Suppress("DEPRECATION")
        fun build() = JiraNodeConfig(
            name = name,
            debug = debug,
            remoteJmx = remoteJmx,
            jvmArgs = jvmArgs,
            splunkForwarder = splunkForwarder,
            collectdConfigs = collectdConfigs,
            launchTimeouts = launchTimeouts
        )
    }

    private companion object {
        private val DEFAULT_COLLECTD_CONFIGS = listOf(
            JiraNodeConfig::class.java.getResource("/collectd/conf/common.conf").toURI(),
            JiraNodeConfig::class.java.getResource("/collectd/conf/jira-default.conf").toURI())
    }
}