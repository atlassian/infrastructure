package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class AdoptOpenJdk11IT {

    @Test
    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            infra.serve().newConnection().use { connection ->
                JstatSupport(AdoptOpenJDK11()).shouldSupportJstat(connection)
            }
        }
    }
}