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


internal class MockHttpServer(private val port: Int) {
    private val handlers: MutableList<RequestHandler> = mutableListOf()

    internal fun register(handler: RequestHandler): URI {
        handlers.add(handler)
        return URI("http://localhost:$port${handler.getContext()}")
    }

    internal fun start(): AutoCloseable {
        val executorService: ExecutorService = Executors.newCachedThreadPool()
        val server = startHttpServer(executorService)
        return AutoCloseable {
            executorService.shutdownNow()
            server.stop(60)
        }
    }

    private fun startHttpServer(executor: Executor): HttpServer {
        val httpServer = retryUntilSuccess(10, TimeUnit.SECONDS) { HttpServer.create(InetSocketAddress(port), 0) }
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
}