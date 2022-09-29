package me.dehasi.jenkins.linter

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class LinterFunctionalTest extends Specification {

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
        result.task(":lint").outcome == UP_TO_DATE
    }
}
