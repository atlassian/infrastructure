package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.ssh.api.SshConnection

/**
 *  Asynchronous profiler. See https://github.com/jvm-profiling-tools/async-profiler#basic-usage
 */
@Deprecated("Use AsyncProfilerHook")
class AsyncProfiler : Profiler {
    override fun install(ssh: SshConnection) {
        ssh.execute("wget -q https://github.com/jvm-profiling-tools/async-profiler/releases/download/v1.4/async-profiler-1.4-linux-x64.tar.gz")
        ssh.execute("mkdir async-profiler")
        ssh.execute("tar -xzf async-profiler-1.4-linux-x64.tar.gz -C async-profiler")
        ssh.execute("sudo sh -c 'echo 1 > /proc/sys/kernel/perf_event_paranoid'")
        ssh.execute("sudo sh -c 'echo 0 > /proc/sys/kernel/kptr_restrict'")
    }

    override fun start(
        ssh: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess {
        ssh.execute("./async-profiler/profiler.sh -b 20000000 start $pid")
        return ProfilerProcess(pid)
    }

    private class ProfilerProcess(private val pid: Int) : RemoteMonitoringProcess {
        private val flameGraphFile = "flamegraph.svg"

        override fun stop(ssh: SshConnection) {
            ssh.execute("./async-profiler/profiler.sh stop $pid -o svg > $flameGraphFile")
        }

        override fun getResultPath(): String {
            return flameGraphFile
        }
    }
}
