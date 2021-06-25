package com.atlassian.performance.tools.infrastructure.api.jira.install

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath

/**
 * Points to an already installed Jira.
 *
 * @since 4.19.0
 */
class InstalledJira(
    /**
     * Contains `./dbconfig.xml` and other server-specific files.
     */
    val home: RemotePath,
    /**
     * Contains `./bin/jira-start.sh` and other install-specific files.
     */
    val installation: RemotePath,
    /**
     * Can run Jira.
     */
    val jdk: JavaDevelopmentKit,
    /**
     * Connects to Jira on HTTP level or below.
     */
    val http: HttpHost
)
