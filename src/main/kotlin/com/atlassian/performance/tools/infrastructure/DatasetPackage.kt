package com.atlassian.performance.tools.infrastructure

import com.atlassian.performance.tools.ssh.SshConnection

interface DatasetPackage {

    /**
     * @return remotely downloaded and unpacked path
     */
    fun download(
        ssh: SshConnection
    ): String
}