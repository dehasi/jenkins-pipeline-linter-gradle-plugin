package me.dehasi.jenkins.linter

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LinterPluginFunctionalTest extends Specification {

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

    def "lint prints hello message"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('lint')
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("Hello from the jenkinsfile")
        result.output.contains("No value set to this property")
        result.task(":lint").outcome == SUCCESS
    }

    def "lint prints parametrised message"() {
        given:
        buildFile << """
            linter {
                 message = 'this is message'
            }
        """
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('lint')
            .withPluginClasspath()
            .build()
        then:
        result.output.contains("Hello from the jenkinsfile")
        result.output.contains("this is message")
        result.task(":lint").outcome == SUCCESS
    }
}
