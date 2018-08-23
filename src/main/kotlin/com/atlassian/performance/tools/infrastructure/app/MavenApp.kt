package com.atlassian.performance.tools.infrastructure.app

import org.jboss.shrinkwrap.resolver.api.maven.Maven
import java.io.File

class MavenApp(
    val groupId: String,
    val artifactId: String,
    val version: String
) : AppSource {
    override fun getLabel(): String {
        return "$groupId:$artifactId:$version"
    }

    override fun acquireFiles(directory: File): List<File> {
        return listOf(
            Maven
                .resolver()
                .resolve("$groupId:$artifactId:$version")
                .withoutTransitivity()
                .asSingleFile()
        )
    }
}