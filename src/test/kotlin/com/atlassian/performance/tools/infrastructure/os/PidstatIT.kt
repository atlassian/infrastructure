package com.atlassian.performance.tools.infrastructure.os

import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import com.atlassian.performance.tools.infrastructure.toSsh
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PidstatIT {
    @Test
    fun shouldProduceLogFile() {
        SshUbuntuContainer.Builder()
            .build()
            .start()
            .use { ubuntu ->
                ubuntu.toSsh().newConnection().use { ssh ->
                    val ubuntu = Ubuntu().also { it.metrics(ssh) }
                    val process = Pidstat.Builder().build().start(ssh)
                    ubuntu.install(ssh, listOf("firefox")) // to produce some CPU, RAM and disc usage
                    val logFile = process.getResultPath()
                    val pidstatOutput = ssh.execute("cat $logFile").output
                    assertThat(pidstatOutput).contains("# Time        UID      TGID       TID    %usr %system  %guest   %wait    %CPU   CPU  minflt/s  majflt/s     VSZ     RSS   %MEM   kB_rd/s   kB_wr/s kB_ccwr/s iodelay   cswch/s nvcswch/s  Command")
                    assertThat(pidstatOutput).contains("firefox")
                }
            }
    }
}
