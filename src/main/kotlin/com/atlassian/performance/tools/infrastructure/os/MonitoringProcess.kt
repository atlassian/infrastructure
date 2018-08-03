package com.atlassian.performance.tools.infrastructure.os

import com.atlassian.performance.tools.ssh.DetachedProcess

data class MonitoringProcess(val process: DetachedProcess, val logFile: String)