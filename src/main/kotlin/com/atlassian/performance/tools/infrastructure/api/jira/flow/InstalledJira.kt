package com.atlassian.performance.tools.infrastructure.api.jira.flow

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit

class InstalledJira(
    val home: String,
    val installation: String,
    val jdk: JavaDevelopmentKit,
    val server: TcpServer
)
