package com.atlassian.performance.tools.infrastructure.virtualusers

import com.atlassian.performance.tools.infrastructure.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.jiraactions.scenario.Scenario
import java.net.URI
import java.time.Duration

class VirtualUsersJar {
    fun testingCommand(
        jdk: JavaDevelopmentKit,
        jarName: String,
        jira: URI,
        minimumRun: Duration,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?
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
            params.add("--scenario=${scenarioClass.canonicalName}"
            )
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
