package com.atlassian.performance.tools.infrastructure.api.jira.hook.install

import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit

class InstalledJira(
    /**
     * E.g. it contains `./dbconfig.xml`
     */
    val home: String,
    /**
     * E.g. it contains `./conf/server.xml`
     */
    val installation: String,
    val jdk: JavaDevelopmentKit,
    val server: TcpServer
)
