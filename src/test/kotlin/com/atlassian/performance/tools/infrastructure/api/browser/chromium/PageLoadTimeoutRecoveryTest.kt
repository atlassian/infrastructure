package com.atlassian.performance.tools.infrastructure.api.browser.chromium

import com.atlassian.performance.tools.infrastructure.sshubuntu.SshUbuntuImage
import com.atlassian.performance.tools.infrastructure.api.browser.Browser
import com.atlassian.performance.tools.infrastructure.browser.SshChromium
import com.atlassian.performance.tools.infrastructure.mock.MockHttpServer
import com.sun.net.httpserver.HttpExchange
import org.assertj.core.api.Assertions
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.TimeUnit

internal class PageLoadTimeoutRecoveryTest {

    internal fun run(chromium: Browser) {
        val remoteChromedriverPort = 9515
        MockHttpServer().start().use { httpServer ->
            val fastResource = httpServer.register(FastResponse())
            val slowResource = httpServer.register(SlowResponse())
            SshUbuntuImage.runSoloUbuntu { ubuntu ->
                val ssh = ubuntu.ssh
                ssh.forwardRemotePort(httpServer.getPort(), httpServer.getPort()).use {
                    val localChromedriverPort = findFreePort()
                    ssh.forwardLocalPort(localChromedriverPort, remoteChromedriverPort).use {
                        ssh.newConnection().use { connection ->
                            chromium.install(connection)
                        }
                        val chromedriverUri = URI("http://localhost:$localChromedriverPort")
                        SshChromium(ssh.newConnection(), chromedriverUri).start().use { sshDriver ->
                            val driver = sshDriver.getDriver()
                            setPageLoadTimeout(driver)

                            driver.get(fastResource.toString())
                            val slowResourceException = Assertions.catchThrowable { driver.get(slowResource.toString()) }
                            Assertions.assertThat(slowResourceException).isInstanceOf(TimeoutException::class.java)
                            val fastResourceException = Assertions.catchThrowable { driver.get(fastResource.toString()) }

                            Assertions.assertThat(fastResourceException).doesNotThrowAnyException()
                        }
                    }
                }
            }
        }
    }

    private fun findFreePort(): Int {
        return ServerSocket(0).use { socket -> return socket.localPort }
    }

    private class FastResponse : MockHttpServer.RequestHandler {
        override fun getContext(): String {
            return "/fast"
        }

        override fun handle(exchange: HttpExchange) {
            val fastResponse = "Fast response"
            exchange.sendResponseHeaders(200, fastResponse.toByteArray().size.toLong())
            val outputStream = exchange.responseBody
            outputStream.write(fastResponse.toByteArray())
            outputStream.close()
        }
    }

    private class SlowResponse : MockHttpServer.RequestHandler {
        override fun getContext(): String {
            return "/slow"
        }

        override fun handle(exchange: HttpExchange) {
            val slowResponse = "Slow response"
            val loading = 4000
            exchange.sendResponseHeaders(200, (slowResponse.toByteArray().size + ".".toByteArray().size * loading).toLong())
            val outputStream = exchange.responseBody
            outputStream.write(slowResponse.toByteArray())
            for (i in 0..1999) {
                outputStream.write(".".toByteArray())
            }
            for (i in 2000 until loading) {
                Thread.sleep(5)
                outputStream.write(".".toByteArray())
            }
            outputStream.close()
        }
    }

    private fun setPageLoadTimeout(driver: RemoteWebDriver) {
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS)
    }
}
