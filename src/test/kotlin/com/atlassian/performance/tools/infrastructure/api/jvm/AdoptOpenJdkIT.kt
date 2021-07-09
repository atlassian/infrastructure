package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class AdoptOpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            JstatSupport(AdoptOpenJDK(), infra.serveTest()).shouldSupportJstat()
        }
    }
}