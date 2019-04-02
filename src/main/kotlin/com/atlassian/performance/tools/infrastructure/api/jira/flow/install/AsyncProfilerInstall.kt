package com.atlassian.performance.tools.infrastructure.api.jira.flow.install

import com.atlassian.performance.tools.infrastructure.api.jira.flow.InstalledJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.flow.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.PassingServe
import com.atlassian.performance.tools.infrastructure.api.jira.flow.serve.Serve
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.PassingStart
import com.atlassian.performance.tools.infrastructure.api.jira.flow.start.Start
import com.atlassian.performance.tools.infrastructure.api.jira.flow.upgrade.Upgrade
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class AsyncProfilerInstall : Install {
    override fun install(
        ssh: SshConnection,
        jira: InstalledJira
    ): Start {
        val directory = "async-profiler"
        val downloads = URI("https://github.com/jvm-profiling-tools/async-profiler/releases/download/")
        val distribution = downloads.resolve("v1.4/async-profiler-1.4-linux-x64.tar.gz")
        ssh.execute("wget -q $distribution")
        ssh.execute("mkdir $directory")
        ssh.execute("tar -xzf async-profiler-1.4-linux-x64.tar.gz -C $directory")
        ssh.execute("sudo sh -c 'echo 1 > /proc/sys/kernel/perf_event_paranoid'")
        ssh.execute("sudo sh -c 'echo 0 > /proc/sys/kernel/kptr_restrict'")
        val profilerPath = "./$directory/profiler.sh"
        return PassingStart(AsyncProfilerUpgrade(profilerPath))
    }
}

private class AsyncProfilerUpgrade(
    private val profilerPath: String
) : Upgrade {

    override fun upgrade(
        ssh: SshConnection,
        jira: StartedJira
    ): Serve {
        ssh.execute("$profilerPath -b 20000000 start ${jira.pid}")
        val report = AsyncProfilerReport(jira.pid, profilerPath)
        return PassingServe(report)
    }
}

private class AsyncProfilerReport(
    private val pid: Int,
    private val profilerPath: String
) : Report {
    override fun locate(ssh: SshConnection): List<String> {
        val flameGraphFile = "flamegraph.svg"
        ssh.execute("$profilerPath stop $pid -o svg > $flameGraphFile")
        return listOf(flameGraphFile)
    }
}
