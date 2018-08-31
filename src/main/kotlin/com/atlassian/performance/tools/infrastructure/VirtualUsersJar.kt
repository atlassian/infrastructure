package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.infrastructure.api.virtualusers.LoadProfile
import com.atlassian.performance.tools.jiraactions.api.scenario.Scenario
import java.net.URI
import java.time.Duration

internal class VirtualUsersJar {
    fun testingCommand(
        jdk: JavaDevelopmentKit,
        jarName: String,
        jira: URI,
        minimumRun: Duration,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    ): String {
        val params = mutableListOf(
            "-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n",
            "-jar $jarName",
            "--jira-address='$jira'",
            "--minimum-run='${minimumRun.toMinutes()}'",
            "--virtual-users='${loadProfile.virtualUsersPerNode}'",
            "--seed=${loadProfile.seed}",
            "--ramp-up-interval=${loadProfile.rampUpInterval}"
        )
        if (scenarioClass != null) {
            params.add("--scenario=${scenarioClass.canonicalName}")
        }
        if (diagnosticsLimit != null) {
            params.add("--diagnostics-limit=$diagnosticsLimit")
        }

        val redirects = listOf(
            "2>virtual-users-error.log",
            "> virtual-users-out.log"
        )
        return jdk.command(
            (params + redirects).joinToString(" ")
        )
    }
}
