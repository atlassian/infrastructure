package com.atlassian.performance.tools.infrastructure.api.jira.node

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost

class JiraNode constructor(
    val host: TcpHost,
    val plan: JiraNodePlan
)