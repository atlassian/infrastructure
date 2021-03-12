package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode
import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNodePlan

interface Infrastructure {
    fun serve(jiraNodePlans: List<JiraNodePlan>): List<JiraNode>
    fun serve(port: Int, name: String): TcpHost
    fun releaseResources()
}