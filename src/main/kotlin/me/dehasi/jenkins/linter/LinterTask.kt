package me.dehasi.jenkins.linter

import org.gradle.api.DefaultTask
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class LinterTask : DefaultTask() {
    @Input abstract fun getPipelinePath(): SetProperty<String>

    @TaskAction fun lint() {
        val paths = getPipelinePath().get()
        println("Validating ${paths.size} files")

        paths
            .map { toAbsoluteFile(it) }
            .filter { fileExists(it) }
            .forEach { validate(it) }
    }

    private fun validate(file: File) {
        println("Validating '$file'")
        file.forEachLine { println(it) }
    }

    private fun fileExists(file: File): Boolean {
        if (!file.exists()) {
            println("File '$file' does not exist. Skipping")
            return false
        }
        if (file.isDirectory) {
            println("File '$file' is a directory. Skipping")
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
