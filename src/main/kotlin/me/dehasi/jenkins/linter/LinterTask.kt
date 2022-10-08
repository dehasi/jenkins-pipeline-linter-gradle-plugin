package me.dehasi.jenkins.linter

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class LinterTask : DefaultTask() {
    @Input abstract fun getPipelinePath(): SetProperty<String>
    @Input abstract fun getJenkinsGateway(): Property<JenkinsGateway>

    private var errors = 0;
    @TaskAction fun lint() {
        val paths = getPipelinePath().get()
        logger.lifecycle("Validating {} files", paths.size)

        errors = 0
        paths
            .map { toAbsoluteFile(it) }
            .filter { fileExists(it) }
            .forEach { validate(it) }
        assert(errors == 0) {
            "$errors during validation jenkins files"
        }
        logger.lifecycle("Validating finished")
    }

    private fun validate(file: File) {
        logger.lifecycle("Validating '{}'", file)
        file.forEachLine { println(it) }
        val jenkinsGateway = getJenkinsGateway().get()
        try {
            val result = jenkinsGateway.validate(file.readText())
            logger.lifecycle(result)
            if (!result.contains("Jenkinsfile successfully validated"))
                ++errors
        } catch (e: Exception) {
            ++errors
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
