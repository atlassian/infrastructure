package com.atlassian.performance.tools.infrastructure.api.profiler

import java.time.Duration

/**
 * A profiler to sample all threads equally every given period of time regardless of thread status.
 */
@Deprecated("Use AsyncProfiler.Builder instead")
class WallClockProfiler : Profiler by AsyncProfiler.Builder()
    .wallClockMode()
    .interval(Duration.ofMillis(9))
    .outputFile("wall-clock-flamegraph.html")
    .build()
