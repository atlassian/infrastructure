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

    internal fun safeReplace(
        connection: SshConnection,
        expression: String,
        output: String,
        file: String
    ): SshConnection.SshResult {
        val escapedExpression = expression.replace("/", "\\/")
        val escapedOutput = output.replace("/", "\\/")
        return connection.safeExecute("sed -i -r 's/$escapedExpression/$escapedOutput/g' $file")
    }

    internal fun safeReplaceXmlTag(
        connection: SshConnection,
        sourceTagName: String,
        replacementString: String,
        file: String
    ): SshConnection.SshResult {
        val escapedSource= sourceTagName.replace("/", "\\/")
        val escapedReplacement= replacementString.replace("/", "\\/")
        return connection.safeExecute("sed -i '/\\/$escapedSource/a\\\n" +
            "            $escapedReplacement\n" +
            "/$escapedSource/, /\\/$escapedSource/d' $file")
    }
}
