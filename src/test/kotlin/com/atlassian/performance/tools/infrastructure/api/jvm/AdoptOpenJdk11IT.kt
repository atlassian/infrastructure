package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            JstatSupport(AdoptOpenJDK11(), infra.serveTest()).shouldSupportJstat()
        }
    }
}
