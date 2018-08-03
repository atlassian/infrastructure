package com.atlassian.performance.tools.infrastructure.jvm

interface JvmDebug {

    fun getRequiredPorts(): List<Int>

    fun getJvmOption(): List<JvmArg>
}