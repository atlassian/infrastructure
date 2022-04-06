package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class OpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            JstatSupport(OpenJDK11(), infra.serveTest()).shouldSupportJstat()
        }
    }
}