package com.atlassian.performance.tools.infrastructure.api.jvm

interface JvmDebug {

    fun getRequiredPorts(): List<Int>

    fun getJvmOption(): List<JvmArg>
}