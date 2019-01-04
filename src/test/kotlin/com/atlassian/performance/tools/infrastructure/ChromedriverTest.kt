package com.atlassian.performance.tools.infrastructure

import org.assertj.core.api.Assertions
import org.junit.Test

class ChromedriverTest {

    private val version = "2.45"

    @Test
    fun shouldInstallChromedriver() {
        UbuntuContainer().run { ssh ->
            Chromedriver(version).install(ssh)
            val result = ssh.safeExecute("./chromedriver --version")
            Assertions.assertThat(result.isSuccessful()).isTrue()
            Assertions.assertThat(result.output).contains(version)
        }
    }
}