package com.atlassian.performance.tools.infrastructure.ubuntu

import com.atlassian.performance.tools.infrastructure.Iostat
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.api.os.Vmstat
import com.atlassian.performance.tools.ssh.api.SshConnection

internal class UbuntuSysstat {

    fun install(
        ssh: SshConnection
    ): List<InstalledOsMetric> {
        val ubuntu = Ubuntu()
        ubuntu.install(ssh, listOf("sysstat"))
        return listOf(Vmstat(), Iostat()).map { InstalledOsMetric(it) }
    }
}
