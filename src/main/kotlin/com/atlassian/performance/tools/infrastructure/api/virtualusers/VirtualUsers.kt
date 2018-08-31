package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.api.MeasurementSource
import com.atlassian.performance.tools.jiraactions.api.scenario.Scenario
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
    fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    )
}