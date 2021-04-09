package com.atlassian.performance.tools.infrastructure.api.jvm

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import org.junit.Test

class OpenJdkIT {

    @Test
    fun shouldSupportJstat() {
        DockerInfrastructure().use { infra ->
            infra.serve().newConnection().use { connection ->
                JstatSupport(OpenJDK()).shouldSupportJstat(connection)
            }
        }
    }
}