package com.atlassian.performance.tools.infrastructure

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.StringReader
import java.net.URI
import javax.json.JsonObject
import javax.json.spi.JsonProvider

/**
 * https://github.com/GoogleChromeLabs/chrome-for-testing
 */
internal object ChromeForTesting {
    fun getLatestDownloadUri(buildVersion: String): URI {
        val json = queryJson("https://googlechromelabs.github.io/chrome-for-testing/latest-patch-versions-per-build-with-downloads.json")
        val uri = json.getJsonObject("builds")
            .getJsonObject(buildVersion)
            .getDownloadUri("linux64")
            ?: throw Exception("Failed to find ChromeDriver download version for $buildVersion. Got response: $json")
        return URI.create(uri)
    }

    fun getLatestStableDownloadUri(): URI {
        val json = queryJson("https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json")
        val uri = json.getJsonObject("channels")
            .getJsonObject("Stable")
            .getDownloadUri("linux64")
            ?: throw Exception("Failed to find stable ChromeDriver download version. Got response: $json")
        return URI.create(uri)
    }

    private fun queryJson(uri: String): JsonObject {
        val response = HttpClients.createDefault().execute(HttpGet(uri))
        return response.entity.content.bufferedReader().use { it.readText() }.let { parse(it) }
    }

    private fun parse(json: String) = JsonProvider.provider()
        .createReaderFactory(null)
        .createReader(StringReader(json))
        .readObject()

    private fun JsonObject.getDownloadUri(platform: String) = getJsonObject("downloads")
        .getJsonArray("chromedriver")
        .find { it.asJsonObject().getString("platform") == platform }
        ?.asJsonObject()?.getString("url")

}