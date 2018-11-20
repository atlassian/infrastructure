package com.atlassian.performance.tools.infrastructure.api.jvm

class JvmArg(
    val key: String,
    val value: String = ""
) {

    override fun toString(): String {
        return "JvmArg(key='$key', value='$value')"
    }
}