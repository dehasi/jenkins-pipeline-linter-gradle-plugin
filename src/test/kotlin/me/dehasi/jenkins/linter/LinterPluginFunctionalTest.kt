package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.Constants.LINT_TASK_NAME
import me.dehasi.jenkins.linter.Constants.SETTINGS_ROOT
import org.gradle.testkit.runner.GradleRunner
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
                 id 'me.dehasi.jenkins-pipeline-linter-gradle-plugin' version 'LATEST-SNAPSHOT'
            }
        """)
    }

    @Test fun `lint processes collection of files`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile1', 'jenkinsfile2', 'jenkinsfile3']
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

    @Test fun `lint prints file content`() {
        val jenkinsFile = File(testProjectDir, "jenkinsfile")
        jenkinsFile.writeText("""
            jenkinsfile content
        """)
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile']
            }
        """)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()

        assert(result.output.contains("jenkinsfile content")) { "result.output=${result.output}" }
        assert(result.task(":${LINT_TASK_NAME}")?.outcome == SUCCESS) { "result.task=" + result.task(":${LINT_TASK_NAME}") }
    }

    @Test fun `lint skips not existing files`() {
        gradleBuildFile.appendText("""
            $SETTINGS_ROOT {
                 pipelinePath = ['jenkinsfile1']
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
}
