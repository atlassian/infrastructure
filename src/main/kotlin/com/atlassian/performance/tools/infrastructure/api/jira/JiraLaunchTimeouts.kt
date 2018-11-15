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
data class JiraLaunchTimeouts(
    val offlineTimeout: Duration,
    val initTimeout: Duration,
    val upgradeTimeout: Duration,
    val unresponsivenessTimeout: Duration
) {
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
}
