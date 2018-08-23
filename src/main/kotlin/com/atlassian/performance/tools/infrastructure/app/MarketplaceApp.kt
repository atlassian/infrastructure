package com.atlassian.performance.tools.infrastructure.app

import org.apache.http.message.BasicHeaderValueParser
import org.apache.http.message.BasicHeaderValueParser.parseHeaderElement
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class MarketplaceApp(
    private val key: String,
    private val build: String
) : AppSource {

    constructor(key: String) : this(key, "latest")

    override fun getLabel(): String {
        return "$key:$build"
    }

    override fun acquireFiles(
        directory: File
    ): List<File> {
        val url = if (build == "latest") {
            URL("https://marketplace.atlassian.com/download/plugins/$key")
        } else {
            URL("https://marketplace.atlassian.com/download/plugins/$key/version/$build")
        }
        val file = downloadToDirectory(url, directory)
        return if (file.extension == "obr") {
            ObrApp(file).extractJars(directory)
        } else {
            listOf(file)
        }
    }

    private fun downloadToDirectory(
        url: URL,
        directory: File
    ): File {
        val connection = url.openConnection()
        connection.connect()
        val file = directory.resolve(connection.attachmentFilename())
        url.openStream().use { Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING) }
        return file
    }

    private fun URLConnection.attachmentFilename(): String {
        val contentDisposition = this.getHeaderField("Content-Disposition")!!
        return parseHeaderElement(contentDisposition, BasicHeaderValueParser.INSTANCE)
            .getParameterByName("filename")
            .value
    }
}