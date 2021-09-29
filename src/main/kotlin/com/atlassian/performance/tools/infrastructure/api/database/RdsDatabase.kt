package com.atlassian.performance.tools.infrastructure.api.database

/**
 * AWS RDS based database.
 */
interface RdsDatabase {

    /**
     * @return Database data location if exists
     */
    fun start(): String

}
