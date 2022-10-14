package me.dehasi.jenkins.linter

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class JenkinsContainer : GenericContainer<JenkinsContainer>("dehasi/jenkins-with-pipeline-model-definition:2.361.1-1") {

    init {
        withEnv("JAVA_OPTS", "-Djenkins.install.runSetupWizard=false")
        withExposedPorts(8080)
        waitingFor(Wait.forHttp("/"))
    }

    fun withUser(username: String, password: String): JenkinsContainer {
        withEnv("JENKINS_OPTS", "" +
                "--argumentsRealm.roles.user=$username " +
                "--argumentsRealm.passwd.admin=$password " +
                "--argumentsRealm.roles.admin=$username")
        return this
    }
}
