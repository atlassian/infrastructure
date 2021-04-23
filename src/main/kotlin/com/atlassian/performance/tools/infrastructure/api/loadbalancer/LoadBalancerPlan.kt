package com.atlassian.performance.tools.infrastructure.api.loadbalancer

import com.atlassian.performance.tools.infrastructure.api.jira.node.JiraNode

interface LoadBalancerPlan {
    fun materialize(nodes: List<JiraNode>): LoadBalancer
}