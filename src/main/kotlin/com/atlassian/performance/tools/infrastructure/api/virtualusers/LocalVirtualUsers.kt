package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.api.main
import org.apache.logging.log4j.LogManager
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
        useLog4jAutomaticConfiguration()
        main(options.toCliArgs())
    }

    private fun useLog4jAutomaticConfiguration() {
        LogManager.getLogger(this::class.java)
    }

    override fun gatherResults() {
        val results = workspace.resolve("virtual-users").resolve("local")

        val workDir = FileSystems.getDefault().getPath(".").toAbsolutePath()

        val filesToMove = listOf(
            "test-results",
            "diagnoses"
        )

        filesToMove.forEach {
            results.resolve(it).ensureDirectory()
            if (workDir.resolve(it).toFile().exists()) {
                Files.move(workDir.resolve(it), results.resolve(it), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}