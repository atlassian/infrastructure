package com.atlassian.performance.tools.infrastructure.api.jira.install.hook

import com.atlassian.performance.tools.infrastructure.api.jira.install.TcpHost
import com.atlassian.performance.tools.infrastructure.api.jira.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.report.Reports
import com.atlassian.performance.tools.infrastructure.api.jira.start.StartedJira
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHook
import com.atlassian.performance.tools.infrastructure.api.jira.start.hook.PostStartHooks
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class AsyncProfilerHook : PreInstallHook {

    override fun call(
        ssh: SshConnection,
        tcp: TcpHost,
        hooks: PreInstallHooks,
        reports: Reports
    ) {
        val directory = "async-profiler"
        val downloads = URI("https://github.com/jvm-profiling-tools/async-profiler/releases/download/")
        val distribution = downloads.resolve("v1.4/async-profiler-1.4-linux-x64.tar.gz")
        ssh.execute("wget -q $distribution")
        ssh.execute("mkdir $directory")
        ssh.execute("tar -xzf async-profiler-1.4-linux-x64.tar.gz -C $directory")
        ssh.execute("sudo sh -c 'echo 1 > /proc/sys/kernel/perf_event_paranoid'")
        ssh.execute("sudo sh -c 'echo 0 > /proc/sys/kernel/kptr_restrict'")
        val profilerPath = "./$directory/profiler.sh"
        val profiler = InstalledAsyncProfiler(profilerPath)
        hooks.postStart.insert(profiler)
    }
}

private class InstalledAsyncProfiler(
    private val profilerPath: String
) : PostStartHook {

    override fun call(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks,
        reports: Reports
    ) {
        ssh.execute("$profilerPath -b 20000000 start ${jira.pid}")
        val profiler = StartedAsyncProfiler(jira.pid, profilerPath)
        reports.add(profiler, jira)
    }
}

private class StartedAsyncProfiler(
    private val pid: Int,
    private val profilerPath: String
) : Report {

    override fun locate(ssh: SshConnection): List<String> {
        val flameGraphFile = "flamegraph.svg"
        ssh.execute("$profilerPath stop $pid -o svg > $flameGraphFile")
        return listOf(flameGraphFile)
    }
}
