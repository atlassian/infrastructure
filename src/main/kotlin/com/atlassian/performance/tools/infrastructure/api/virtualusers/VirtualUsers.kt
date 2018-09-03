package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.api.MeasurementSource
import com.atlassian.performance.tools.jiraactions.scenario.Scenario
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import java.net.URI

/**
 * Represents virtual users capable of applying load on a provided application.
 */
interface VirtualUsers : MeasurementSource {

    /**
     * Applies load with [loadProfile] on [jira].
     *
     * @param jira instance address to apply load on
     * @param loadProfile to be applied
     * @param scenarioClass test scenario class to be executed
     */
    @Deprecated(
        message = "Do not use.",
        replaceWith = ReplaceWith(
            expression = "applyLoad(options = VirtualUserOptions())",
            imports = ["com.atlassian.performance.tools.virtualusers.VirtualUserOptions"]
        )
    )
    @Suppress("DEPRECATION")
    fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?
    ): Unit = applyLoad(
        jira,
        loadProfile,
        scenarioClass,
        diagnosticsLimit = null
    )

    /**
     * Applies load with [loadProfile] on [jira].
     *
     * @param jira instance address to apply load on
     * @param loadProfile to be applied
     * @param scenarioClass test scenario class to be executed
     * @param diagnosticsLimit limiting how many times diagnostics can be executed
     */
    @Deprecated(
        message = "Do not use.",
        replaceWith = ReplaceWith(
            expression = "applyLoad(options = VirtualUserOptions())",
            imports = ["com.atlassian.performance.tools.virtualusers.VirtualUserOptions"]
        )
    )
    fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    )

    /**
     * Applies load described by [options]
     *
     * @param options for the load
     */
    fun applyLoad(
        options: VirtualUserOptions
    )
}