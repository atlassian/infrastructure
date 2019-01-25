package com.atlassian.performance.tools.infrastructure.api.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MarketplaceAppIT {

    @Test
    fun shouldDownload() {
        val jar = MarketplaceApp("com.atlassian.labs.rest-api-browser", "30210")
                .acquireFiles(createTempDir())
                .first()
        assertThat(jar.name).isEqualTo("rest-api-browser-3.2.1.jar")
        assertThat(jar.length()).isEqualTo(1208069)
    }
}