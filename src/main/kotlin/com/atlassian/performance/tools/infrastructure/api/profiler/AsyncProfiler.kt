package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration.ofSeconds

/**
 *  Asynchronous profiler. See https://github.com/jvm-profiling-tools/async-profiler#basic-usage
 */
class AsyncProfiler : Profiler {

    private val release = "async-profiler-2.9-linux-x64"

    override fun install(ssh: SshConnection) {
        val tarGz = "$release.tar.gz"
        ssh.execute("wget -q https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/$tarGz")
        ssh.execute("tar --extract --gzip --file $tarGz")
        ssh.execute("sudo sh -c 'echo 1 > /proc/sys/kernel/perf_event_paranoid'")
        ssh.execute("sudo sh -c 'echo 0 > /proc/sys/kernel/kptr_restrict'")
    }

    override fun start(
        ssh: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess {
        val script = "./$release/profiler.sh"
        IdempotentAction("start async-profiler") {
            ssh.execute("$script start $pid")
        }.retry(2, StaticBackoff(ofSeconds(5)))
        return ProfilerProcess(script, pid)
    }

    private class ProfilerProcess(
        private val script: String,
        private val pid: Int
    ) : RemoteMonitoringProcess {
        private val flameGraphFile = "flamegraph.svg"

        override fun stop(ssh: SshConnection) {
            ssh.execute("$script stop $pid -o svg > $flameGraphFile")
        }

        override fun getResultPath(): String {
            return flameGraphFile
        }
    }
}