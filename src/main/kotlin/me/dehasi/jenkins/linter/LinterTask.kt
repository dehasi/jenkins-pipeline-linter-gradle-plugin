package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure
import me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.WARNING
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class LinterTask : DefaultTask() {
    @Input abstract fun getPipelinePath(): SetProperty<String>
    @Input abstract fun getActionOnFailure(): Property<ActionOnFailure>
    @Input abstract fun getJenkinsGateway(): Property<JenkinsGateway>

    private var errorCount = 0;
    @TaskAction fun lint() {
        val paths = getPipelinePath().get()
        logger.lifecycle("Validating {} files", paths.size)

        errorCount = 0
        paths
            .map { toAbsoluteFile(it) }
            .filter { fileExists(it) }
            .forEach { validate(it) }
        logger.lifecycle("Validation finished with $errorCount errors.")

        if (errorCount != 0) {
            val message = "$errorCount errors encountered validating files."
            if (getActionOnFailure().get() == WARNING) logger.warn(message)
            else throw RuntimeException(message)
        }
    }

    private fun validate(file: File) {
        logger.lifecycle("Validating '{}'", file)
        val jenkinsGateway = getJenkinsGateway().get()
        try {
            val result = jenkinsGateway.validate(file.readText())
            logger.lifecycle(result)
            if (!result.contains("Jenkinsfile successfully validated"))
                ++errorCount
        } catch (e: Exception) {
            ++errorCount
            logger.error("{}", e)
        }
    }

    private fun fileExists(file: File): Boolean {
        if (!file.exists()) {
            logger.warn("File '{}' does not exist. Skipping", file)
            return false
        }
        if (file.isDirectory) {
            logger.warn("File '{}' is a directory. Skipping", file)
            return false
        }
        return true
    }

    private fun toAbsoluteFile(path: String): File {
        val file = File(path)
        return if (file.isAbsolute) file
        else project.file(path).absoluteFile
    }
}
