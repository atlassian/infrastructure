package com.atlassian.performance.tools.infrastructure.api.profiler

import com.atlassian.performance.tools.infrastructure.api.process.RemoteMonitoringProcess
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.jvmtasks.api.StaticBackoff
import com.atlassian.performance.tools.ssh.api.SshConnection
import java.time.Duration
import java.time.Duration.ofSeconds

/**
 *  Asynchronous profiler. See https://github.com/jvm-profiling-tools/async-profiler#basic-usage
 */
class AsyncProfiler private constructor(
    private val startParams: List<String>,
    private val stopParams: List<String>,
    private val outputFile: String
) : Profiler {

    @Deprecated("Use AsyncProfiler.Builder instead")
    constructor() : this(emptyList(), emptyList(), "flamegraph.html")

    private val release = "async-profiler-2.10-linux-x64"

    override fun install(ssh: SshConnection) {
        val tarGz = "$release.tar.gz"
        ssh.execute("wget -q https://github.com/async-profiler/async-profiler/releases/download/v2.10/$tarGz")
        ssh.execute("tar --extract --gzip --file $tarGz")
        ssh.execute("sudo sh -c 'echo 1 > /proc/sys/kernel/perf_event_paranoid'")
        ssh.execute("sudo sh -c 'echo 0 > /proc/sys/kernel/kptr_restrict'")
    }

    override fun start(
        ssh: SshConnection,
        pid: Int
    ): RemoteMonitoringProcess {
        val script = "./$release/profiler.sh"
        val params = startParams.joinToString(separator = " ")
        IdempotentAction("start async-profiler") {
            ssh.execute("$script start $params $pid")
        }.retry(2, StaticBackoff(ofSeconds(5)))
        return ProfilerProcess(script, pid, stopParams, outputFile)
    }

    private class ProfilerProcess(
        private val script: String,
        private val pid: Int,
        private val stopParams: List<String>,
        private val outputFile: String
    ) : RemoteMonitoringProcess {

        override fun stop(ssh: SshConnection) {
            val params = stopParams.joinToString(separator = " ")
            ssh.execute("$script stop $pid $params", timeout = ofSeconds(50))
        }

        override fun getResultPath(): String {
            return outputFile
        }
    }

    /**
     * @since 4.27.0
     */
    class Builder {

        private var outputFormat: String = "flamegraph"
        private var outputFile: String = "flamegraph.html"
        private val startParams = mutableListOf<String>()
        private val stopParams = mutableListOf<String>()

        fun outputFile(outputFile: String) = apply { this.outputFile = outputFile }

        /**
         * See [profiler options](https://github.com/async-profiler/async-profiler/tree/v2.9#profiler-options).
         *
         * @param outputFormat -o
         * @param outputFile -f
         * @since 4.28.0
         */
        fun output(outputFormat: String, outputFile: String) = apply {
            this.outputFormat = outputFormat
            this.outputFile = outputFile
        }

        /**
         * @since 4.28.0
         */
        fun jfr(outputFile: String) = output("jfr", outputFile)

        /**
         * @since 4.28.0
         */
        fun flamegraph(outputFile: String) = output("flamegraph", outputFile)

        fun wallClockMode() = apply {
            startParams.add("-e")
            startParams.add("wall")
        }

        fun interval(interval: Duration) = apply {
            startParams.add("-i")
            if (interval < ofSeconds(1)) {
                startParams.add(interval.nano.toString())
            } else {
                throw Exception("The interval $interval seems to big. Usually it's counted in milliseconds or nanoseconds. Try an interval under a second.")
            }
        }

        @Deprecated("use startParams instead", ReplaceWith("startParams(extraParams)"))
        fun extraParams(vararg extraParams: String) = startParams(*extraParams)

        /**
         * @since 4.28.0
         */
        fun startParams(vararg startParams: String) = apply {
            this.startParams.addAll(startParams)
        }

        /**
         * @since 4.28.0
         */
        fun stopParams(vararg stopParams: String) = apply {
            this.stopParams.addAll(stopParams)
        }

        fun build(): Profiler {
            val startParamsCopy = startParams + "-o $outputFormat -f $outputFile"
            val stopParamsCopy =  stopParams + "-o $outputFormat -f $outputFile"
            return AsyncProfiler(startParamsCopy, stopParamsCopy, outputFile)
        }
    }
}