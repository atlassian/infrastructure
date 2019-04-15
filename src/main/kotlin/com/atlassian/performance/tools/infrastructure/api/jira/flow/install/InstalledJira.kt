package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit

class InstalledJira(
    val home: String,
    val installation: String,
    val jdk: JavaDevelopmentKit,
    val server: TcpServer
)
