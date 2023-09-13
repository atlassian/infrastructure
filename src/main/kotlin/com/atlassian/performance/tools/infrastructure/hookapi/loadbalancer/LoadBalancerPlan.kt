package com.atlassian.performance.tools.infrastructure.hookapi.loadbalancer

import com.atlassian.performance.tools.infrastructure.api.jira.install.HttpNode
import com.atlassian.performance.tools.infrastructure.hookapi.jira.start.hook.PreStartHooks

interface LoadBalancerPlan {
    fun materialize(nodes: List<HttpNode>, hooks: List<PreStartHooks>): HttpNode
}
