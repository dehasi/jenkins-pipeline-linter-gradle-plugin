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


        project.tasks.register(LINT_TASK_NAME, LinterTask::class.java) {
            logLinterParams(extension)
            assertRequiredFields(extension)
            it.getPipelinePath().set(extension.pipelinePath.get())
            it.getActionOnFailure().set(extension.actionOnFailure.get())
            val jenkins = extension.getJenkinsExtension()
            it.getJenkinsGateway()
                .set(jenkinsGateway(
                    httpClient(ignoreCertificate = jenkins.ignoreCertificate.get()),
                    jenkins.url.get(), Credentials(jenkins.username.get(), jenkins.password.get())))
        }
    }

    private fun assertRequiredFields(extension: LinterExtension) {
        check(extension.pipelinePath.get().isNotEmpty()) { "pipelinePath needs to be set." }

        check(extension.getJenkinsExtension().url.get().isNotEmpty()) { "jenkins.url needs to be set." }
        check(extension.getJenkinsExtension().username.get().isNotEmpty()) { "jenkins.username needs to be set." }
        check(extension.getJenkinsExtension().password.get().isNotEmpty()) { "jenkins.password needs to be set." }
    }

    private fun logLinterParams(extension: LinterExtension) {
        log.info("pipelinePath={}", extension.pipelinePath.get())
        log.info("actionOnFailure={}", extension.actionOnFailure.get())
        logJenkinsParams(extension.getJenkinsExtension())
    }

    private fun logJenkinsParams(jenkins: JenkinsExtension) {
        log.info("url={}", jenkins.url.get())
        log.info("username={}", jenkins.username.get())
        log.info("password={}", jenkins.password.get())
        log.info("ignoreCertificate={}", jenkins.ignoreCertificate.get())
        log.info("trustSelfSigned={}, trustSelfSigned is not implemented yet", jenkins.trustSelfSigned.get())
        log.info("useCrumbIssuer={}, useCrumbIssuer is not implemented yet", jenkins.useCrumbIssuer.get())

        if (jenkins.trustSelfSigned.get()) log.warn("trustSelfSigned is not implemented yet")
        if (jenkins.useCrumbIssuer.get()) log.warn("useCrumbIssuer is not implemented yet")
    }
}
