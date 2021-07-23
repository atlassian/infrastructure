package com.atlassian.performance.tools.infrastructure.api.jira.instance

import com.atlassian.performance.tools.infrastructure.api.distribution.PublicJiraSoftwareDistribution
import com.atlassian.performance.tools.infrastructure.api.jira.EmptyJiraHome
import com.atlassian.performance.tools.infrastructure.api.jira.install.JiraInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.ParallelInstallation
import com.atlassian.performance.tools.infrastructure.api.jira.install.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraLaunchScript
import com.atlassian.performance.tools.infrastructure.api.jira.start.JiraStart
import com.atlassian.performance.tools.infrastructure.api.jvm.OracleJDK
import com.atlassian.performance.tools.infrastructure.api.network.HttpServerRoom
import com.atlassian.performance.tools.infrastructure.jira.install.hook.HookedJiraInstallation
import com.atlassian.performance.tools.infrastructure.jira.start.hook.HookedJiraStart
import net.jcip.annotations.NotThreadSafe

/**
 * Specifies how a Jira node should be built.
 * Does not build the node, due to various possible ordering, concurrency and optimizations.
 * Actual provisioning is reserved for a [JiraInstancePlan].
 *
 * @constructor specifies the plan, but doesn't hold any resources
 * @see JiraInstancePlan
 * @since 4.19.0
 */
class JiraNodePlan private constructor(
    internal val serverRoom: HttpServerRoom,
    internal val installation: JiraInstallation,
    internal val start: JiraStart,
    internal val hooks: PreInstallHooks
) {

    @NotThreadSafe
    class Builder(
        private var serverRoom: HttpServerRoom
    ) {
        private var installation: JiraInstallation = ParallelInstallation(
            EmptyJiraHome(),
            PublicJiraSoftwareDistribution("7.13.0"),
            OracleJDK()
        )
        private var start: JiraStart = JiraLaunchScript()
        private var hooks: PreInstallHooks = PreInstallHooks.default()

        constructor(plan: JiraNodePlan) : this(
            plan.serverRoom
        ) {
            this.installation = plan.installation
            this.start = plan.start
            this.hooks = plan.hooks
        }

        fun serverRoom(serverRoom: HttpServerRoom) = apply { this.serverRoom = serverRoom }
        fun installation(installation: JiraInstallation) = apply { this.installation = installation }
        fun start(start: JiraStart) = apply { this.start = start }
        fun hooks(hooks: PreInstallHooks) = apply { this.hooks = hooks }
        fun dataCenter() = hooks(PreInstallHooks.default().apply { postInstall.insert(DefaultClusterProperties()) })

        fun build() = JiraNodePlan(
            serverRoom,
            HookedJiraInstallation(installation, hooks),
            HookedJiraStart(start, hooks.preStart),
            hooks
        )
    }
}
