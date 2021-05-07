package com.atlassian.performance.tools.infrastructure.api.jira.start

import com.atlassian.performance.tools.infrastructure.api.jira.install.InstalledJira
import net.jcip.annotations.ThreadSafe

@ThreadSafe
interface JiraStart {

    fun start(
        installed: InstalledJira
    ): StartedJira
}
