package com.atlassian.performance.tools.infrastructure.jira.install

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.os.RemotePath

/**
 * Points to an already installed Jira.
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
     * Hosts Jira. Specifies sockets used by Jira to handle requests.
     */
    val server: TcpServer
)
