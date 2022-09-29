package me.dehasi.jenkins.linter

import org.gradle.api.Plugin
import org.gradle.api.Project

class LinterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("linter", LinterExtension::class.java)

        project.tasks.register("lint", LinterTask::class.java) {
            it.getMassage().set(extension.message.get())
        }
    }
}
