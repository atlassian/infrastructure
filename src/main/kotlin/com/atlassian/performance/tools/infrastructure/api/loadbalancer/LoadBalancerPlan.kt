package com.atlassian.performance.tools.infrastructure.api.loadbalancer

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PreStartHooks

interface LoadBalancerPlan {
    fun materialize(nodes: List<HttpNode>, hooks: List<PreStartHooks>): LoadBalancer
}