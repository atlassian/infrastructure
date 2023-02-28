package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.ssh.api.SshConnection
import java.nio.file.Files

class MinimalMysqlJiraHome : JiraHomeSource {
    override fun download(ssh: SshConnection): String {
        val temporaryDirectory = Files.createTempDirectory("jpt-jirahome").toFile()
        this::class.java.classLoader.getResourceAsStream("mysql-dbconfig.xml")!!.use { inputStream ->
            temporaryDirectory.resolve("dbconfig.xml").outputStream().use { inputStream.copyTo(it) }
        }
        ssh.upload(temporaryDirectory, "jirahome")
        return "jirahome"
    }
}