package com.atlassian.performance.tools.infrastructure.api.jvm

interface VersionedJavaDevelopmentKit : JavaDevelopmentKit {
    fun getMajorVersion(): Int
}