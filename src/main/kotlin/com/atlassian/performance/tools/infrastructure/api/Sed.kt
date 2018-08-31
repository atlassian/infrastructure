package com.atlassian.performance.tools.infrastructure.api

import com.atlassian.performance.tools.ssh.api.SshConnection

class Sed {
    fun replace(
        connection: SshConnection,
        expression: String,
        output: String,
        file: String
    ) {
        val escapedExpression = expression.replace("/", "\\/")
        val escapedOutput = output.replace("/", "\\/")
        connection.execute("sed -i -r 's/$escapedExpression/$escapedOutput/g' $file")
    }
}