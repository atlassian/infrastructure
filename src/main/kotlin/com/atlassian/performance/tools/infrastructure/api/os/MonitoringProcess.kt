package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.ssh.api.DetachedProcess

class MonitoringProcess(val process: DetachedProcess, val logFile: String) {

    override fun toString(): String {
        return "MonitoringProcess(process=$process, logFile='$logFile')"
    }
}