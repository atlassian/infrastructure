package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.jvmtasks.api.TaskTimer
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions


interface Keb {
    fun start(ssh: SshConnection)

    /**
     * @return list of paths on the VU host that can be moved to the results directory
     */
    fun getArtifact(): List<String>

}

class SshVirtualUsers2(
    private val name: String,
    private val ssh: Ssh,
    private val preload: List<Keb>,
    private val virtualUsers: VirtualUsers,
    private val resultsTransport: ResultsTransport
) : VirtualUsers {
    override fun applyLoad(options: VirtualUserOptions) {
        ssh.newConnection().use { ssh ->
            preload.forEach { it.start(ssh) }
        }
        virtualUsers.applyLoad(options)
    }

    override fun gatherResults() {
        TaskTimer.time("gather results from virtual users") {

            val uploadDirectory = "results"
            val resultsDirectory = "$uploadDirectory/virtual-users/$name"
            ssh.newConnection().use { ssh ->
                ssh.safeExecute("mkdir -p $resultsDirectory")
                preload.forEach { keb ->
                    keb.getArtifact().forEach { artifact ->
                        ssh.safeExecute("mv $artifact $resultsDirectory")
                    }
                }
                ssh.safeExecute("find $resultsDirectory -empty -type f -delete")
                resultsTransport.transportResults(
                    targetDirectory = uploadDirectory,
                    sshConnection = ssh
                )
            }
            virtualUsers.gatherResults()
        }
    }

}


fun main(args: Array<String>) {

}
