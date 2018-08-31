package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.infrastructure.api.jira.SetenvSh.Variables.*
import com.atlassian.performance.tools.infrastructure.api.jvm.JvmArg
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * https://confluence.atlassian.com/adminjiraserver072/increasing-jira-application-memory-828788060.html
 * https://confluence.atlassian.com/adminjiraserver072/setting-properties-and-options-on-startup-828788225.html
 */
class SetenvSh(
    jiraInstallation: String
) {
    private enum class Variables {
        JVM_MINIMUM_MEMORY,
        JVM_MAXIMUM_MEMORY,
        JVM_SUPPORT_RECOMMENDED_ARGS
    }

    val location = "$jiraInstallation/bin/setenv.sh"

    private val sed = Sed()

    fun setup(
        connection: SshConnection,
        config: JiraNodeConfig,
        gcLog: JiraGcLog,
        jiraIp: String
    ) {
        val args = config.jvmArgs
        val original = connection.execute("cat ${this.location}").output
        setMemory(connection, args, original)
        val jvmArgs = args.arguments(
            debug = config.debug,
            jmx = config.remoteJmx,
            jiraIp = jiraIp
        ) + gcLog.jvmArg()
        setArguments(connection, jvmArgs, original)
    }

    private fun assertVariableExists(
        original: String,
        variable: Variables
    ) {
        if (!original.contains("$variable=")) {
            throw Exception("${this.location} has no '$variable' variable definition")
        }
    }

    private fun setMemory(
        connection: SshConnection,
        jvmArgs: JiraJvmArgs,
        original: String
    ) {
        assertVariableExists(original, JVM_MINIMUM_MEMORY)
        sed.replace(
            connection = connection,
            expression = "^$JVM_MINIMUM_MEMORY=.*$",
            output = "$JVM_MINIMUM_MEMORY=\"${jvmArgs.xms}\"",
            file = this.location
        )

        assertVariableExists(original, JVM_MAXIMUM_MEMORY)
        sed.replace(
            connection = connection,
            expression = "^$JVM_MAXIMUM_MEMORY=.*$",
            output = "$JVM_MAXIMUM_MEMORY=\"${jvmArgs.xmx}\"",
            file = this.location
        )
    }

    private fun setArguments(
        connection: SshConnection,
        jvmArgs: List<JvmArg>,
        original: String
    ) {
        assertVariableExists(original, JVM_SUPPORT_RECOMMENDED_ARGS)
        val existingJvmArgs = jvmArgs.filter { original.contains(it.key) }
        val newJvmArgs = jvmArgs.filter { !original.contains(it.key) }
        existingJvmArgs.forEach { replaceArgument(connection, it) }
        newJvmArgs.forEach { appendArgument(connection, it) }
    }

    private fun replaceArgument(
        connection: SshConnection,
        arg: JvmArg
    ) {
        sed.replace(
            connection = connection,
            expression = "${arg.key}[^ ]+",
            output = "${arg.key}${arg.value}",
            file = this.location
        )
    }

    private fun appendArgument(
        connection: SshConnection,
        arg: JvmArg
    ) {
        sed.replace(
            connection = connection,
            expression = "^($JVM_SUPPORT_RECOMMENDED_ARGS.*)\"$",
            output = "\\1 ${arg.key}${arg.value}\"",
            file = this.location
        )
    }
}