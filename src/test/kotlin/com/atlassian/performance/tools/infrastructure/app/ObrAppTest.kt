package com.atlassian.performance.tools.infrastructure.app

import com.atlassian.performance.tools.io.ensureDirectory
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File

class ObrAppTest {

    @Test
    fun extractedJiraPortfolioObrContainsExpectedJars() {
        val obrLocation = "com/atlassian/performance/tools/infrastructure/jira/jira-portfolio-2.12.1.obr"
        val obrFileName = javaClass.classLoader.getResource(obrLocation).file
        val tempDir = createTempDir().ensureDirectory()

        ObrApp(File(obrFileName)).extractJars(tempDir)

        val extractedJars = tempDir.listFiles().map { it.name }
        assertThat(extractedJars, containsInAnyOrder(
            "jira-portfolio-2.12.1.jar",
            "portfolio-plugin-2.12.1.jar",
            "querydsl-4.0.7-provider-plugin-1.1.jar",
            "team-management-plugin-2.12.1.jar"
        ))
    }
}