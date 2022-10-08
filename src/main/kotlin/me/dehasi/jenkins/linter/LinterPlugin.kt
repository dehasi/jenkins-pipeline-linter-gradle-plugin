package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import me.dehasi.jenkins.linter.DependencyInjection.httpClient
import me.dehasi.jenkins.linter.DependencyInjection.jenkinsGateway
import org.gradle.api.Plugin
import org.gradle.api.Project

class LinterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(SETTINGS_ROOT, LinterExtension::class.java)

        val jenkins = extension.getJenkinsExtension()

        project.tasks.register(LINT_TASK_NAME, LinterTask::class.java) {
            it.getPipelinePath().set(extension.pipelinePath.get())
            println("url=${jenkins.url.get()}")
            println("username=${jenkins.username.get()}")
            println("password=${jenkins.password.get()}")
            it.getJenkinsGateway()
                .set(jenkinsGateway(
                    httpClient(jenkins.username.get(), jenkins.password.get()),
                    jenkins.url.get()))
        }
    }
}
