package com.atlassian.performance.tools.infrastructure.api.jira

import com.atlassian.performance.tools.infrastructure.mock.UnimplementedSshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class MinimalMysqlJiraHomeTest {

    @Test
    fun `uploads jirahome only with dbconfig xml`() {
        val ssh = SshMock()

        MinimalMysqlJiraHome().download(ssh)

        val jirahome = ssh.lastUpload!!
        val content = jirahome.listFiles()!!
        assertThat(content.map { it.name }, equalTo(listOf("dbconfig.xml")))
    }

    private class SshMock : SshConnection by UnimplementedSshConnection() {
        var lastUpload: File? = null

        override fun upload(localSource: File, remoteDestination: String) {
            this.lastUpload = localSource
        }
    }
}