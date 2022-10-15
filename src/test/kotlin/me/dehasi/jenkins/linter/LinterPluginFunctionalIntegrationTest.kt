package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import me.dehasi.jenkins.linter.JenkinsContainer.Companion.HTTPS_PORT
import me.dehasi.jenkins.linter.Jenkinsfiles.CORRECT_JENKINSFILE_CONTENT
import me.dehasi.jenkins.linter.Jenkinsfiles.INCORRECT_JENKINSFILE_CONTENT
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
internal class LinterPluginFunctionalIntegrationTest {

    private val USERNAME = "admin"
    private val PASSWORD = "password"

    @Container private val jenkins = JenkinsContainer().apply {
        withUser(USERNAME, PASSWORD)
        withTLS()
    }

    @TempDir lateinit var testProjectDir: File
    lateinit var gradleBuildFile: File

    @BeforeEach fun `create gradle build file`() {
        gradleBuildFile = File(testProjectDir, "build.gradle")
        gradleBuildFile.writeText("""
            plugins {
                 id 'me.dehasi.jenkins-pipeline-linter' version 'LATEST-SNAPSHOT'
            }
        """)
    }

    @Test fun `lint sends correct file to jenkins and print result`() {
        val jenkinsFile = File(testProjectDir, "jenkinsfile")
        jenkinsFile.writeText(CORRECT_JENKINSFILE_CONTENT)

        gradleBuildFile.appendText("""
            import static me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.WARNING
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 actionOnFailure = WARNING
                 jenkins {
                    url = 'https://localhost:${jenkins.getMappedPort(HTTPS_PORT)}'
                    username = '$USERNAME'
                    password = '$PASSWORD'
                    ignoreCertificate()
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("Jenkinsfile successfully validated.")) { "result.output=${result.output}" }
        assert(result.output.contains("Validation finished with 0 errors.")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }

    @Test fun `lint incorrect file, actionOnFailure = WARNING, print result`() {
        val jenkinsFile = File(testProjectDir, "jenkinsfile")
        jenkinsFile.writeText(INCORRECT_JENKINSFILE_CONTENT)

        gradleBuildFile.appendText("""
            import static me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.WARNING
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 actionOnFailure = WARNING
                 jenkins {
                    url = 'https://localhost:${jenkins.getMappedPort(HTTPS_PORT)}'
                    username = '$USERNAME'
                    password = '$PASSWORD'
                    ignoreCertificate()
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("Errors encountered validating Jenkinsfile")) { "result.output=${result.output}" }
        assert(result.output.contains("Validation finished with 1 errors.")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }

    @Test fun `lint incorrect file, actionOnFailure = FAIL_BUILD, fails build`() {
        val jenkinsFile = File(testProjectDir, "jenkinsfile")
        jenkinsFile.writeText(INCORRECT_JENKINSFILE_CONTENT)

        gradleBuildFile.appendText("""
            import static me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.FAIL_BUILD
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 actionOnFailure = FAIL_BUILD
                 jenkins {
                    url = 'https://localhost:${jenkins.getMappedPort(HTTPS_PORT)}'
                    username = '$USERNAME'
                    password = '$PASSWORD'
                    ignoreCertificate()
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail()

        assert(result.output.contains("Errors encountered validating Jenkinsfile")) { "result.output=${result.output}" }
        assert(result.output.contains("Validation finished with 1 errors.")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == FAILED) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }
}
