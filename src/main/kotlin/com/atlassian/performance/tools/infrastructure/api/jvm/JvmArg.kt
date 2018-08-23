package com.atlassian.performance.tools.infrastructure.api.jvm

data class JvmArg(
    val key: String,
    val value: String = ""
)