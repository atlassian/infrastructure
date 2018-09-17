package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.api.Sed
import com.atlassian.performance.tools.ssh.api.SshConnection


class AtlassianSplunkForwarder(
    private val additionalEventFields: Map<String, String> = mapOf("serviceId" to "90c41131-2dc2-4cb5-9806-c01b6659817e"),
    private val kinesisRoleArn: String = "arn:aws:iam::915926889391:role/pipeline-prod-log-producer-545459181881"
) : SplunkForwarder {

    override fun run(sshConnection: SshConnection, name: String) {
        run(sshConnection, name, "/home/ubuntu/jirahome/log")
    }

    override fun run(sshConnection: SshConnection, name: String, logsPath: String) {
        val logstashImage = DockerImage("docker.elastic.co/logstash/logstash-oss:6.2.4")
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