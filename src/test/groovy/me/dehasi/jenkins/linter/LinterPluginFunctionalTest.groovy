package me.dehasi.jenkins.linter

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LinterPluginFunctionalTest extends Specification {

    private static final String SETTINGS_ROOT = "linter"
    private static final String LINT_TASK_NAME = "lint"

    @TempDir File testProjectDir
    File buildFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                 id 'me.dehasi.jenkins-pipeline-linter-gradle-plugin' version 'LATEST-SNAPSHOT'
            }
        """
    }

    def "lint processes collection of files"() {
        given:
        buildFile << """
            ${SETTINGS_ROOT} {
                 pipelinePath = ['jenkinsfile1', 'jenkinsfile2', 'jenkinsfile3']
            }
        """
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("Validating 3 files")
        result.task(":${LINT_TASK_NAME}").outcome == SUCCESS
    }

    def "lint prints file content"() {
        given:
        File jenkinsFile = new File(testProjectDir, 'jenkinsfile')
        jenkinsFile << """
            jenkinsfile content
        """
        buildFile << """
            ${SETTINGS_ROOT} {
                 pipelinePath = ['jenkinsfile']
            }
        """
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("jenkinsfile content")
        result.task(":${LINT_TASK_NAME}").outcome == SUCCESS
    }

    def "lint skips not existing files"() {
        given:
        buildFile << """
            ${SETTINGS_ROOT} {
                 pipelinePath = ['jenkinsfile1']
            }
        """
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("File '${testProjectDir.canonicalPath}/jenkinsfile1' does not exist. Skipping")
        result.task(":${LINT_TASK_NAME}").outcome == SUCCESS
    }

    def "lint skips directories"() {
        given:
        File dir = new File(testProjectDir, 'dir1')
        assert dir.mkdir(), "cant create a directory from file '${dir}'"

        buildFile << """
            ${SETTINGS_ROOT} {
                 pipelinePath = ['dir1']
            }
        """
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(LINT_TASK_NAME)
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("File '${testProjectDir.canonicalPath}/dir1' is a directory. Skipping")
        result.task(":${LINT_TASK_NAME}").outcome == SUCCESS
    }
}
