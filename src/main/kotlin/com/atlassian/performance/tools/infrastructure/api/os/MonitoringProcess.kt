package com.atlassian.performance.tools.infrastructure.api.os

import com.atlassian.performance.tools.ssh.api.DetachedProcess

data class MonitoringProcess(val process: DetachedProcess, val logFile: String)