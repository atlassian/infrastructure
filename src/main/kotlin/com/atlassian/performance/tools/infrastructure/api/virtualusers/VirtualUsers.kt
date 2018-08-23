package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.api.MeasurementSource
import com.atlassian.performance.tools.jiraactions.scenario.Scenario
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
     */
    fun applyLoad(jira: URI, loadProfile: LoadProfile, scenarioClass: Class<out Scenario>?)
}