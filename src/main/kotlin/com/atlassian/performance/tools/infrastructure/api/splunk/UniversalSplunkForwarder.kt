package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.infrastructure.DockerImage
import com.atlassian.performance.tools.infrastructure.splunk.Log4jJsonifier
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 * Since 4.21.0, it's compatible with Ubuntu 16.04, 18.04, 20.04 and 22.04.
 */
class UniversalSplunkForwarder(
    private val splunkServerIp: String,
    private val managementPort: Int = 8089,
    private val httpEventCollectorPort: Int = 8088,
    private val indexingReceiverPort: Int = 9997
) : SplunkForwarder {

    override fun run(sshConnection: SshConnection, name: String, logsPath: String) {
        val splunkForwarderImage = DockerImage("splunk/universalforwarder:6.5.3-monitor")

        val parameters = listOf(
            "--hostname $name",
            "-p $managementPort:$managementPort",
            "-p $httpEventCollectorPort:$httpEventCollectorPort",
            "-p $indexingReceiverPort:$indexingReceiverPort",
            "--env SPLUNK_FORWARD_SERVER='$splunkServerIp:$indexingReceiverPort'",
            "--env SPLUNK_ADD='monitor /var/log/jiralogs/'",
            "--env SPLUNK_START_ARGS='--accept-license'",
            "--env SPLUNK_USER=root",
            "--volume $logsPath:/var/log/jiralogs/",
            "--volume /var/lib/docker/containers:/host/containers:ro",
            "--volume /var/log:/docker/log:ro",
            "--volume /var/run/docker.sock:/var/run/docker.sock:ro")

        splunkForwarderImage.run(sshConnection, parameters.joinToString(" "))
    }

    override fun jsonifyLog4j(sshConnection: SshConnection, log4jPropertiesPath: String) {
        Log4jJsonifier().jsonifyLog4j1(sshConnection, log4jPropertiesPath)
    }

    override fun getRequiredPorts(): List<Int> {
        return listOf(managementPort, httpEventCollectorPort, indexingReceiverPort)
    }

}


