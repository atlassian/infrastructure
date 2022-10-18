package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.jvm.DisabledJvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.jvm.VersionedJavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmDebug
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.DisabledRemoteJmx
import com.atlassian.performance.tools.infrastructure.api.jvm.jmx.RemoteJmx
import com.atlassian.performance.tools.infrastructure.api.profiler.Profiler
import com.atlassian.performance.tools.infrastructure.api.splunk.DisabledSplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.Log4j2SplunkForwarder
import com.atlassian.performance.tools.infrastructure.api.splunk.SplunkForwarder
import com.atlassian.performance.tools.infrastructure.profiler.DisabledProfiler
import java.net.URI

class JiraNodeConfig private constructor(
    val name: String,
    val debug: JvmDebug,
    val remoteJmx: RemoteJmx,
    val jvmArgs: JiraJvmArgs,
    val collectdConfigs: List<URI>,
    val splunkForwarder: SplunkForwarder,
    val launchTimeouts: JiraLaunchTimeouts,
    val jdk: JavaDevelopmentKit,
    val profiler: Profiler
) {

    @Deprecated(message = "Use JiraNodeConfig.Builder instead.")
    constructor(
        name: String,
        debug: JvmDebug,
        remoteJmx: RemoteJmx,
        jvmArgs: JiraJvmArgs,
        collectdConfigs: List<URI>,
        splunkForwarder: SplunkForwarder,
        launchTimeouts: JiraLaunchTimeouts
    ) : this(
        name = name,
        debug = debug,
        remoteJmx = remoteJmx,
        jvmArgs = jvmArgs,
        collectdConfigs = collectdConfigs,
        splunkForwarder = splunkForwarder,
        launchTimeouts = launchTimeouts,
        jdk = OracleJDK(),
        profiler = DisabledProfiler()
    )

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
        launchTimeouts = launchTimeouts,
        jdk = OracleJDK(),
        profiler = DisabledProfiler()
    )

    @Deprecated(message = "Use JiraNodeConfig.Builder instead.")
    constructor(
        name: String,
        debug: JvmDebug,
        remoteJmx: RemoteJmx,
        jvmArgs: JiraJvmArgs,
        splunkForwarder: SplunkForwarder,
        launchTimeouts: JiraLaunchTimeouts
    ) : this(
        name = name,
        debug = debug,
        remoteJmx = remoteJmx,
        jvmArgs = jvmArgs,
        collectdConfigs = DEFAULT_COLLECTD_CONFIGS,
        splunkForwarder = splunkForwarder,
        launchTimeouts = launchTimeouts,
        jdk = OracleJDK(),
        profiler = DisabledProfiler()
    )

    @Deprecated(
        message = "Use JiraNodeConfig.Builder instead.",
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
        return "JiraNodeConfig(name='$name', debug=$debug, remoteJmx=$remoteJmx, jvmArgs=$jvmArgs, collectdConfigs=$collectdConfigs, splunkForwarder=$splunkForwarder, launchTimeouts=$launchTimeouts, jdk=$jdk, profiler=$profiler)"
    }

    class Builder() {
        private var name: String = "jira-node"
        private var debug: JvmDebug = DisabledJvmDebug()
        private var remoteJmx: RemoteJmx = DisabledRemoteJmx()
        private var jvmArgs: JiraJvmArgs = JiraJvmArgs()
        private var collectdConfigs: List<URI> = DEFAULT_COLLECTD_CONFIGS
        private var splunkForwarder: SplunkForwarder = DisabledSplunkForwarder()
        private var launchTimeouts: JiraLaunchTimeouts = JiraLaunchTimeouts.Builder().build()
        private var jdk: JavaDevelopmentKit = OracleJDK()
        private var versionedJdk: VersionedJavaDevelopmentKit? = null
        private var profiler: Profiler = DisabledProfiler()

        constructor(
            jiraNodeConfig: JiraNodeConfig
        ) : this() {
            name = jiraNodeConfig.name
            debug = jiraNodeConfig.debug
            remoteJmx = jiraNodeConfig.remoteJmx
            jvmArgs = jiraNodeConfig.jvmArgs
            splunkForwarder = jiraNodeConfig.splunkForwarder
            launchTimeouts = jiraNodeConfig.launchTimeouts
            jdk = jiraNodeConfig.jdk
            profiler = jiraNodeConfig.profiler
        }

        fun name(name: String) = apply { this.name = name }
        fun debug(debug: JvmDebug) = apply { this.debug = debug }
        fun remoteJmx(remoteJmx: RemoteJmx) = apply { this.remoteJmx = remoteJmx }
        fun jvmArgs(jvmArgs: JiraJvmArgs) = apply { this.jvmArgs = jvmArgs }
        fun splunkForwarder(splunkForwarder: SplunkForwarder) = apply { this.splunkForwarder = splunkForwarder }
        fun launchTimeouts(launchTimeouts: JiraLaunchTimeouts) = apply { this.launchTimeouts = launchTimeouts }
        fun collectdConfig(collectdConfigs: List<URI>) = apply { this.collectdConfigs = collectdConfigs }
        fun jdk(jdk: JavaDevelopmentKit) = apply { this.jdk = jdk }
        fun versionedJdk(versionedJdk: VersionedJavaDevelopmentKit) = apply { this.versionedJdk = versionedJdk }
        fun profiler(profiler: Profiler) = apply { this.profiler = profiler }

        fun build() = JiraNodeConfig(
            name = name,
            debug = debug,
            remoteJmx = remoteJmx,
            jvmArgs = jvmArgs,
            splunkForwarder = Log4j2SplunkForwarder("log4j2.xml", splunkForwarder),
            collectdConfigs = collectdConfigs,
            launchTimeouts = launchTimeouts,
            jdk = if (versionedJdk != null) versionedJdk as JavaDevelopmentKit else jdk,
            profiler = profiler
        )
    }

    private companion object {
        private val DEFAULT_COLLECTD_CONFIGS = listOf(
            JiraNodeConfig::class.java.getResource("/collectd/conf/common.conf").toURI(),
            JiraNodeConfig::class.java.getResource("/collectd/conf/jira-default.conf").toURI()
        )
    }
}
