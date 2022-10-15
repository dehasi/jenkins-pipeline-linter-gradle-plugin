package me.dehasi.jenkins.linter

import org.testcontainers.containers.BindMode.READ_ONLY
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

class JenkinsContainer : GenericContainer<JenkinsContainer>("dehasi/jenkins-with-pipeline-model-definition:2.361.1-1") {
    companion object {
        const val HTTP_PORT = 8080
        const val HTTPS_PORT = 8443
    }

    init {
        withEnv("JAVA_OPTS", "-Djenkins.install.runSetupWizard=false")
        withExposedPorts(HTTP_PORT, HTTPS_PORT)
        waitingFor(Wait.forHttp("/"))
    }

    fun withUser(username: String, password: String): JenkinsContainer {
        val ops = envMap["JENKINS_OPTS"].orEmpty()
        withEnv("JENKINS_OPTS", ops +
                "--argumentsRealm.roles.user=$username " +
                "--argumentsRealm.passwd.admin=$password " +
                "--argumentsRealm.roles.admin=$username ")
        return this
    }

    fun withTLS(): JenkinsContainer {
        val ops = envMap["JENKINS_OPTS"].orEmpty()
        withEnv("JENKINS_OPTS", ops +
                "--httpPort=$HTTP_PORT " +
                "--httpsPort=$HTTPS_PORT " +
                "--httpsKeyStorePassword=password " +
                "--httpsKeyStore=/var/lib/jenkins/jenkins.jks ")

        withClasspathResourceMapping("jenkins.jks", "/var/lib/jenkins/jenkins.jks", READ_ONLY)
        return this
    }
}
