package com.atlassian.performance.tools.infrastructure

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class ParallelExecutor {
    private companion object {
        private val counter = AtomicInteger()
    }

    private val executorNumber = counter.incrementAndGet()

    internal fun execute(
        vararg callables: () -> Unit
    ) {
        val executor = Executors.newCachedThreadPool(
            ThreadFactoryBuilder()
                .setNameFormat("parallel-executor-$executorNumber-thread-%d")
                .build()
        )
        try {
            callables.map {
                executor.submit(it)
            }.forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }
    }
}
