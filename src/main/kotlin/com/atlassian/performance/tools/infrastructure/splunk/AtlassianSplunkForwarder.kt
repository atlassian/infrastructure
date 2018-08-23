package com.atlassian.performance.tools.infrastructure.splunk

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.os.Sed
import com.atlassian.performance.tools.io.readResourceText
import com.atlassian.performance.tools.ssh.SshConnection


class AtlassianSplunkForwarder(
) : SplunkForwarder {
    override fun run(sshConnection: SshConnection, name: String) {
        val logstashImage = DockerImage("docker.elastic.co/logstash/logstash-oss:6.2.4")
        val jiraLogsPath = "/home/ubuntu/jirahome/log/"
        val logstashConfFilePath = "~/logstash.conf"

        createLogstashConfigFile(sshConnection, logstashConfFilePath)

        val parameters = listOf(
            "--volume $jiraLogsPath:/usr/share/logstash/pipeline/",
            "--volume /var/lib/docker/containers:/host/containers:ro",
            "--volume /var/run/docker.sock:/var/run/docker.sock:ro",
            "-v $logstashConfFilePath:/usr/share/logstash/config/logstash.conf")

        val arguments = "sh -c \"logstash-plugin install logstash-output-kinesis; " +
            "bin/logstash -f /usr/share/logstash/config/logstash.conf --config.reload.automatic; " +
            "/usr/local/bin/docker-entrypoint\""

        logstashImage.run(sshConnection, parameters.joinToString(" "), arguments)
    }

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        Sed().replace(
            connection = sshConnection,
            expression = "NewLineIndentingFilteringPatternLayout",
            output = "layout.JsonLayout",
            file = log4jPropertiesPath
        )
    }

    override fun getRequiredPorts(): List<Int> {
        return emptyList()
    }

    private fun createLogstashConfigFile(sshConnection: SshConnection, logstashConfFilePath: String) {
        val logstashConf = readResourceText("splunk/logstash.conf")
        sshConnection.execute(
            """cat > $logstashConfFilePath <<'EOF'
                |$logstashConf
                |EOF""".trimMargin()
        )
    }
}