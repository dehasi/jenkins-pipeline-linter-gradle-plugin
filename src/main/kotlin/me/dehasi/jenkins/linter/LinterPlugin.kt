package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import org.gradle.api.Plugin
import org.gradle.api.Project

class LinterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(SETTINGS_ROOT, LinterExtension::class.java)

        project.tasks.register(LINT_TASK_NAME, LinterTask::class.java) {
            it.getPipelinePath().set(extension.pipelinePath.get())
        }
    }
}
