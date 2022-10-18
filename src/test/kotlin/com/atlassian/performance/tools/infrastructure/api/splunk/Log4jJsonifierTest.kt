package com.atlassian.performance.tools.infrastructure.api.splunk

import com.atlassian.performance.tools.ssh.api.SshConnection
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.Mockito


internal class Log4jJsonifierTest {
    companion object {
        private const val LOG4J1_PROPERTIES = "jira-log4j.properties"
        private const val LOG4J2_CONFIG = "jira-log4j2.xml"

        private const val LOG4J1_REPLACEMENT_CMD =
            "sed -i -r 's/NewLineIndentingFilteringPatternLayout/layout.JsonLayout/g'"
        private const val LOG4J2_REPLACEMENT_CMD = "sed -i '/\\/PatternLayout/a\\\n" +
            "            <AtlassianJsonLayout  filteringApplied=\"false\"\\/>\n" +
            "/PatternLayout/, /\\/PatternLayout/d'"
    }

    private var mockConnection: SshConnection = Mockito.mock(SshConnection::class.java)
    private var unsuccessfulResult: SshConnection.SshResult = SshConnection.SshResult(-1, "", "")
    private var successfulResult: SshConnection.SshResult = SshConnection.SshResult(0, "", "")

    @Test
    fun testJsonifyLog4j1Old() {
        Log4jJsonifier().jsonifyLog4j1(mockConnection, LOG4J1_PROPERTIES)

        Mockito.verify(mockConnection)
            .execute("$LOG4J1_REPLACEMENT_CMD $LOG4J1_PROPERTIES")
    }

    @Test
    fun testJsonifyLog4j1And2BothSucceed() {
        Mockito.`when`(mockConnection.safeExecute(Mockito.anyString()))
            .thenReturn(successfulResult)

        Log4jJsonifier().jsonifyLog4j1AndLog4j2(mockConnection, LOG4J1_PROPERTIES, LOG4J2_CONFIG)

        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J1_REPLACEMENT_CMD $LOG4J1_PROPERTIES")
        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J2_REPLACEMENT_CMD $LOG4J2_CONFIG")
    }

    @Test
    fun testJsonifyLog4j1And2Only1Succeeds() {
        Mockito.`when`(mockConnection.safeExecute(Mockito.anyString()))
            .thenReturn(successfulResult)
            .thenReturn(unsuccessfulResult)

        Log4jJsonifier().jsonifyLog4j1AndLog4j2(mockConnection, LOG4J1_PROPERTIES, LOG4J2_CONFIG)

        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J1_REPLACEMENT_CMD $LOG4J1_PROPERTIES")
        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J2_REPLACEMENT_CMD $LOG4J2_CONFIG")
    }

    @Test
    fun testJsonifyLog4j1And2Only2Succeeds() {
        Mockito.`when`(mockConnection.safeExecute(Mockito.anyString()))
            .thenReturn(unsuccessfulResult)
            .thenReturn(successfulResult)

        Log4jJsonifier().jsonifyLog4j1AndLog4j2(mockConnection, LOG4J1_PROPERTIES, LOG4J2_CONFIG)

        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J1_REPLACEMENT_CMD $LOG4J1_PROPERTIES")
        Mockito.verify(mockConnection)
            .safeExecute("$LOG4J2_REPLACEMENT_CMD $LOG4J2_CONFIG")
    }

    @Test
    fun testJsonifyLog4j1And2BothFail() {
        Mockito.`when`(mockConnection.safeExecute(Mockito.anyString()))
            .thenReturn(unsuccessfulResult)

        assertThatThrownBy {
            Log4jJsonifier().jsonifyLog4j1AndLog4j2(
                mockConnection,
                LOG4J1_PROPERTIES,
                LOG4J2_CONFIG
            )
        }
            .isInstanceOf(Exception::class.java)
    }
}
