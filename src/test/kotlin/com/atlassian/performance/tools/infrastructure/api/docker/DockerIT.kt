package com.atlassian.performance.tools.infrastructure.api.docker

import com.atlassian.performance.tools.infrastructure.api.DockerInfrastructure
import com.atlassian.performance.tools.infrastructure.api.os.Ubuntu
import org.junit.Test
import java.time.Duration

class DockerIT {
    @Test
    fun installWorks() {
        DockerInfrastructure().use { infra ->
            infra.serveTest().newConnection().use { connection ->
                //workaround for a bug in Docker download site for bionic
                val packageFile = "containerd.io_1.2.2-3_amd64.deb"
                Ubuntu().install(connection,listOf( "curl"))
                connection.execute("curl -O https://download.docker.com/linux/ubuntu/dists/bionic/pool/edge/amd64/$packageFile", Duration.ofMinutes(3))
                connection.execute("sudo apt install ./$packageFile", Duration.ofMinutes(3))

                Docker.Builder().build().install(connection)
                DockerImage.Builder("hello-world").build().run(connection)
            }
        }
    }
}
