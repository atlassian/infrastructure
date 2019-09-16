package com.atlassian.performance.tools.infrastructure.mock

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


internal class MockHttpServer {
    private val handlers: MutableList<RequestHandler> = mutableListOf()

    internal fun start(): CloseableHttpServer {
        val executorService: ExecutorService = Executors.newCachedThreadPool()
        val server = startHttpServer(executorService)
        return CloseableHttpServer(
            httpServer = server,
            executorService = executorService
        )
    }

    private fun startHttpServer(executor: Executor): HttpServer {
        val httpServer = retryUntilSuccess(10, TimeUnit.SECONDS) { HttpServer.create(InetSocketAddress(0), 0) }
        httpServer.executor = executor

        handlers.forEach { handler ->
            httpServer.createContext(handler.getContext()).handler = handler
        }

        httpServer.start()
        return httpServer
    }

    internal interface RequestHandler : HttpHandler {
        fun getContext(): String
    }

    internal class CloseableHttpServer(
        private val httpServer: HttpServer,
        private val executorService: ExecutorService
    ) : AutoCloseable {

        override fun close() {
            executorService.shutdownNow()
            httpServer.stop(60)
        }

        fun register(handler: RequestHandler): URI {
            httpServer.createContext(handler.getContext()).handler = handler
            return URI("http://localhost:${this.getPort()}${handler.getContext()}")
        }

        fun getPort(): Int {
            return httpServer.address.port
        }
    }
}