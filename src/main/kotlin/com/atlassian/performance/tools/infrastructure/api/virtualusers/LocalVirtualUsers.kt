package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.io.ensureDirectory
import com.atlassian.performance.tools.jiraactions.scenario.Scenario
import com.atlassian.performance.tools.virtualusers.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.main
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * [VirtualUsers] running on a local machine.
 */
class LocalVirtualUsers(
    private val workspace: Path
) : VirtualUsers {

    override fun applyLoad(options: VirtualUserOptions) {
        main(options.toCliArgs())
    }

    @Deprecated(message = "Not implemented!")
    override fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    ) {
        throw RuntimeException("Deprecated method not implemented!")
    }

    override fun gatherResults() {
        val results = workspace.resolve("virtual-users").resolve("local")

        val workDir = FileSystems.getDefault().getPath(".").toAbsolutePath()

        val filesToMove = listOf(
            "test-results",
            "diagnoses",
            "virtual-users.log",
            "virtual-users-out.log",
            "virtual-users-error.log"
        )

        filesToMove.forEach {
            results.resolve(it).ensureDirectory()
            if (workDir.resolve(it).toFile().exists()) {
                Files.move(workDir.resolve(it), results.resolve(it), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}