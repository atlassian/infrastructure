package com.atlassian.performance.tools.infrastructure.api.jira

import java.time.Duration

/**
 * [JiraLaunchTimeouts] specifying JPT patience during Jira launch period
 *
 * @param offlineTimeout is the duration JPT waits when there’s no connectivity at all, e.g. Tomcat is down
 * @param initTimeout is the duration JPT waits when there’s connectivity but Jira still starts and cannot successfully handle requests
 * @param upgradeTimeout is the duration JPT waits for Jira to finish upgrade tasks
 * @param unresponsivenessTimeout is the duration JPT would tolerate Jira not responding to status query on upgrades endpoint
 */
class JiraLaunchTimeouts @Deprecated(message = "Use JiraLaunchTimeouts.Builder instead.") constructor(
    val offlineTimeout: Duration,
    val initTimeout: Duration,
    val upgradeTimeout: Duration,
    val unresponsivenessTimeout: Duration
) {

    /**
     * [JiraLaunchTimeouts] specifying JPT patience during Jira launch period
     *
     * @param offlineTimeout is the duration JPT waits when there’s no connectivity at all, e.g. Tomcat is down
     * @param initTimeout is the duration JPT waits when there’s connectivity but Jira still starts and cannot successfully handle requests
     * @param upgradeTimeout is the duration JPT waits for Jira to finish upgrade tasks
     */
    @Suppress("DEPRECATION")
    @Deprecated(message = "Use JiraLaunchTimeouts.Builder instead")
    constructor(
        offlineTimeout: Duration,
        initTimeout: Duration,
        upgradeTimeout: Duration
    ) : this(
        offlineTimeout = offlineTimeout,
        initTimeout = initTimeout,
        upgradeTimeout = upgradeTimeout,
        unresponsivenessTimeout = Duration.ofMinutes(4)
    )

    override fun toString(): String {
        return "JiraLaunchTimeouts(offlineTimeout=$offlineTimeout, initTimeout=$initTimeout, upgradeTimeout=$upgradeTimeout, unresponsivenessTimeout=$unresponsivenessTimeout)"
    }

    class Builder() {
        private var offlineTimeout: Duration = Duration.ofMinutes(8)
        private var initTimeout: Duration = Duration.ofMinutes(4)
        private var upgradeTimeout: Duration = Duration.ofMinutes(8)
        private var unresponsivenessTimeout: Duration = Duration.ofMinutes(4)

        constructor(jiraLaunchTimeouts: JiraLaunchTimeouts) : this() {
            offlineTimeout = jiraLaunchTimeouts.offlineTimeout
            initTimeout = jiraLaunchTimeouts.initTimeout
            upgradeTimeout = jiraLaunchTimeouts.upgradeTimeout
            unresponsivenessTimeout = jiraLaunchTimeouts.unresponsivenessTimeout
        }

        /**
         * @param offlineTimeout is the duration JPT waits when there’s no connectivity at all, e.g. Tomcat is down
         */
        fun offlineTimeout(offlineTimeout: Duration) = apply { this.offlineTimeout = offlineTimeout }

        /**
         * @param initTimeout is the duration JPT waits when there’s connectivity but Jira still starts and cannot successfully handle requests
         */
        fun initTimeout(initTimeout: Duration) = apply { this.initTimeout = initTimeout }

        /**
         * @param upgradeTimeout is the duration JPT waits for Jira to finish upgrade tasks
         */
        fun upgradeTimeout(upgradeTimeout: Duration) = apply { this.upgradeTimeout = upgradeTimeout }

        /**
         * @param unresponsivenessTimeout is the duration JPT would tolerate Jira not responding to status query on upgrades endpoint
         */
        fun unresponsivenessTimeout(unresponsivenessTimeout: Duration) = apply { this.unresponsivenessTimeout = unresponsivenessTimeout }

        @Suppress("DEPRECATION")
        fun build() = JiraLaunchTimeouts(
            offlineTimeout = offlineTimeout,
            initTimeout = initTimeout,
            upgradeTimeout = upgradeTimeout,
            unresponsivenessTimeout = unresponsivenessTimeout
        )
    }
}
