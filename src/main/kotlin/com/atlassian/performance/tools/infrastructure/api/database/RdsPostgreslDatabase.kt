package com.atlassian.performance.tools.infrastructure.api.database

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class RdsPostgreslDatabase(
) : RdsDatabase {
    private val logger: Logger = LogManager.getLogger(this::class.java)


    override fun start(): String {
        // TODO: call aws-resource?
        return "OK"
    }

    private fun isReady() {
        // TODO: call aws-resource in a sleep loop
    }
}