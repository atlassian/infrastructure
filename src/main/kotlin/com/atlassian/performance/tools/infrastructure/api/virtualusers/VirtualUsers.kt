package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.infrastructure.api.MeasurementSource
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions

/**
 * Represents virtual users capable of applying load on a provided application.
 */
interface VirtualUsers : MeasurementSource {

    /**
     * Applies load described by [options]
     *
     * @param options for the load
     */
    fun applyLoad(
        options: VirtualUserOptions
    )
}