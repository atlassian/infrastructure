package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.infrastructure.api.jvm.JavaDevelopmentKit
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions

internal class VirtualUsersJar {
    fun testingCommand(
        jarName: String,
        jdk: JavaDevelopmentKit,
        options: VirtualUserOptions
    ): String {
        val javaParams = mutableListOf(
            "-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n",
            "-jar $jarName"
        )
        val cliArgs = options.toCliArgs()
        val redirects = listOf(
            "2>virtual-users-error.log",
            "> virtual-users-out.log"
        )
        return jdk.command(
            (javaParams + cliArgs + redirects).joinToString(" ")
        )
    }
}
