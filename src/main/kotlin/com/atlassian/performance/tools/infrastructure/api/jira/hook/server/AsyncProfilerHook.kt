package com.atlassian.performance.tools.infrastructure.api.jira.hook.server

import com.atlassian.performance.tools.infrastructure.api.jira.hook.PostStartHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.TcpServer
import com.atlassian.performance.tools.infrastructure.api.jira.hook.PreInstallHooks
import com.atlassian.performance.tools.infrastructure.api.jira.hook.report.Report
import com.atlassian.performance.tools.infrastructure.api.jira.hook.start.PostStartHook
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.net.URI

class AsyncProfilerHook : PreInstallHook {

    override fun run(
        ssh: SshConnection,
        server: TcpServer,
        hooks: PreInstallHooks
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
        hooks.hook(profiler)
    }
}

private class InstalledAsyncProfiler(
    private val profilerPath: String
) : PostStartHook {

    override fun run(
        ssh: SshConnection,
        jira: StartedJira,
        hooks: PostStartHooks
    ) {
        ssh.execute("$profilerPath -b 20000000 start ${jira.pid}")
        val profiler = StartedAsyncProfiler(jira.pid, profilerPath)
        hooks.addReport(profiler)
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
