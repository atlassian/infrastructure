package com.atlassian.performance.tools.infrastructure.api.app

import com.atlassian.performance.tools.infrastructure.ObrApp
import java.io.File

class MavenDownloadedApps(
    private val appsDirectory: File
) {
    fun list() = appsDirectory.listFiles().map { LocalApp(it) }

    class LocalApp internal constructor(
        private val app: File
    ) : AppSource {
        override fun getLabel(): String {
            return app.name
        }

        override fun acquireFiles(directory: File): List<File> {
            return if (app.extension == "obr") {
                ObrApp(app).extractJars(directory)
            } else {
                val target = directory.resolve(app.name)
                app.copyTo(target = target, overwrite = true)
                listOf(target)
            }
        }
    }
}