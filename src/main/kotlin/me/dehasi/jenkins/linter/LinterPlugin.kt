package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import me.dehasi.jenkins.linter.DependencyInjection.httpClient
import me.dehasi.jenkins.linter.DependencyInjection.jenkinsGateway
import me.dehasi.jenkins.linter.LinterExtension.JenkinsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class LinterPlugin : Plugin<Project> {
    private val log: Logger = Logging.getLogger(LinterTask::class.java)

    override fun apply(project: Project) {
        val extension = project.extensions.create(SETTINGS_ROOT, LinterExtension::class.java)
        val jenkins = extension.getJenkinsExtension()

        project.tasks.register(LINT_TASK_NAME, LinterTask::class.java) {
            it.getPipelinePath().set(extension.pipelinePath.get())
            logParams(jenkins)
            it.getJenkinsGateway()
                .set(jenkinsGateway(
                    httpClient(jenkins.username.get(), jenkins.password.get()),
                    jenkins.url.get()))
        }
    }

    private fun logParams(jenkins: JenkinsExtension) {
        log.info("url={}", jenkins.url.get())
        log.info("username={}", jenkins.username.get())
        log.info("password={}", jenkins.password.get())
    }
}
