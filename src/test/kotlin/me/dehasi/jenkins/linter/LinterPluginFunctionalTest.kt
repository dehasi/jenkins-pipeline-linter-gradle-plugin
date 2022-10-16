package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class LinterPluginFunctionalTest {

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

    @Test fun `lint processes collection of files`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile1', 'jenkinsfile2', 'jenkinsfile3']
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("Validating 3 files")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }

    @Test fun `lint prints all settings`() {
        gradleBuildFile.appendText("""
            import static me.dehasi.jenkins.linter.LinterExtension.ActionOnFailure.WARNING
            $SETTINGS_ROOT {
                 pipelinePath = ['path/to/jenkinsfile']
                 actionOnFailure = WARNING
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                    ignoreCertificate()
                    trustSelfSigned()
                    useCrumbIssuer()
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME, "--info")
            .withPluginClasspath()
            .build()

        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }

        assert(result.output.contains("pipelinePath=[path/to/jenkinsfile]")) { "result.output=${result.output}" }
        assert(result.output.contains("actionOnFailure=WARNING")) { "result.output=${result.output}" }

        assert(result.output.contains("url=http://jenkins.example")) { "result.output=${result.output}" }
        assert(result.output.contains("username=jenkins_username")) { "result.output=${result.output}" }
        assert(result.output.contains("password=jenkins_password")) { "result.output=${result.output}" }

        assert(result.output.contains("ignoreCertificate=true")) { "result.output=${result.output}" }
        assert(result.output.contains("trustSelfSigned=true, trustSelfSigned is not implemented yet")) { "result.output=${result.output}" }
        assert(result.output.contains("useCrumbIssuer=true, useCrumbIssuer is not implemented yet")) { "result.output=${result.output}" }
    }

    @Test fun `lint skips not existing files`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile1']
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("File '${testProjectDir.canonicalPath}/jenkinsfile1' does not exist. Skipping")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }

    }

    @Test fun `lint skips directories`() {
        val dir = File(testProjectDir, "dir1")
        assert(dir.mkdir()) { "cant create a directory from file '${dir}'" }
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['dir1']
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("File '${testProjectDir.canonicalPath}/dir1' is a directory. Skipping")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }

    @Test fun `lint pipelinePath is not set fails build`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = []
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail()

        assert(result.output.contains("pipelinePath needs to be set.")) { "result.output=${result.output}" }
    }

    @Test fun `lint url is not set fails build`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 jenkins {
                    // url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail()

        assert(result.output.contains("jenkins.url needs to be set.")) { "result.output=${result.output}" }
    }

    @Test fun `lint username is not set fails build`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 jenkins {
                    url = 'http://jenkins.example'
                    // username = 'jenkins_username'
                    password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail()

        assert(result.output.contains("jenkins.username needs to be set.")) { "result.output=${result.output}" }
    }

    @Test fun `lint password is not set fails build`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
                 jenkins {
                    url = 'http://jenkins.example'
                    username = 'jenkins_username'
                    // password = 'jenkins_password'
                 }
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .buildAndFail()

        assert(result.output.contains("jenkins.password needs to be set.")) { "result.output=${result.output}" }
    }
}
