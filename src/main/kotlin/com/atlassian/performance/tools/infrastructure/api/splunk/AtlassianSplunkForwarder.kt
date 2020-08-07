package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.splunk.Log4jJsonifier
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration

/**
 * Runs on Ubuntu 18.04, 20.04 and 22.04.
 */
class AtlassianSplunkForwarder(
    private val additionalEventFields: Map<String, String>,
    private val kinesisRoleArn: String
) : SplunkForwarder {

    override fun run(sshConnection: SshConnection, name: String, logsPath: String) {
        val logstashImage = DockerImage("docker.elastic.co/logstash/logstash-oss:6.2.4", Duration.ofMinutes(5))
        val logstashConfFilePath = "~/logstash.conf"

        sshConnection.execute("""cat > $logstashConfFilePath <<'EOF'
        |${LogStashConfigBuilder(additionalEventFields, kinesisRoleArn).build()}
        |EOF""".trimMargin())


        val parameters = listOf(
            "--volume $logsPath:/usr/share/logstash/pipeline/",
            "--volume /var/lib/docker/containers:/host/containers:ro",
            "--volume /var/run/docker.sock:/var/run/docker.sock:ro",
            "-v $logstashConfFilePath:/usr/share/logstash/config/logstash.conf")

        val arguments = "sh -c \"logstash-plugin install logstash-output-kinesis; " +
            "bin/logstash -f /usr/share/logstash/config/logstash.conf --config.reload.automatic; " +
            "/usr/local/bin/docker-entrypoint\""

        logstashImage.run(sshConnection, parameters.joinToString(" "), arguments)
    }

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        Log4jJsonifier().jsonifyLog4j1(sshConnection, log4jPropertiesPath)
    }

    override fun getRequiredPorts(): List<Int> {
        return emptyList()
    }
}

internal class LogStashConfigBuilder(private val additionalEventFields: Map<String, String>, private val kinesisRoleArn: String) {
    private fun input(): String {
        return """input {
        |   file {
        |       path => "/usr/share/logstash/pipeline/*"
        |       start_position => "beginning"
        |   }
        |}""".trimIndent().trimMargin()
    }

    private fun filter(additionalEventFields: Map<String, String>): String {
        var additionalEventFieldsString = ""
        for ((k, v) in additionalEventFields) {
            additionalEventFieldsString += "\"$k\" => \"$v\"\n"
        }

        return """filter {
        |   json{
        |       source => "message"
        |       target => "message_json"
        |   }
        |   mutate {
        |       add_field => {
        |           $additionalEventFieldsString
        |       }
        |   }
        |}""".trimIndent().trimMargin()
    }


    private fun output(kinesisRoleArn: String): String {
        return """output {
        |   kinesis {
        |       role_arn => "$kinesisRoleArn"
        |       metrics_level => "none"
        |       stream_name => "prod-logs"
        |       region => "us-east-1"
        |       randomized_partition_key => true
        |   }
        |}""".trimIndent().trimMargin()
    }

    fun build(): String {
        return input() + "\n" + filter(additionalEventFields) + "\n" + output(kinesisRoleArn)
    }
}
