package com.atlassian.performance.tools.infrastructure.app

import java.io.File

interface AppSource {
    fun acquireFiles(directory: File): List<File>

    fun getLabel(): String
}