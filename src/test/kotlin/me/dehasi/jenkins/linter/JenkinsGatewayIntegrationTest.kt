package me.dehasi.jenkins.linter

import me.dehasi.jenkins.linter.DependencyInjection.httpClient
import me.dehasi.jenkins.linter.DependencyInjection.jenkinsGateway
import me.dehasi.jenkins.linter.JenkinsContainer.Companion.HTTPS_PORT
import me.dehasi.jenkins.linter.Jenkinsfiles.CORRECT_JENKINSFILE_CONTENT
import me.dehasi.jenkins.linter.Jenkinsfiles.INCORRECT_JENKINSFILE_CONTENT
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class JenkinsGatewayIntegrationTest {

    private val USERNAME = "admin"
    private val PASSWORD = "admin"

    @Container private val jenkins = JenkinsContainer()
        .withUser(USERNAME, PASSWORD)
        .withTLS()

    private lateinit var jenkinsGateway: JenkinsGateway

    @BeforeEach fun createJenkinsGateway() {
        jenkinsGateway = jenkinsGateway(httpClient(USERNAME, PASSWORD, ignoreCertificate = true),
            "https://localhost:${jenkins.getMappedPort(HTTPS_PORT)}")
    }

    @Test fun validate_validJenkinsfile_reportsSuccess() {
        val response = jenkinsGateway.validate(CORRECT_JENKINSFILE_CONTENT)

        assert(response.contains("Jenkinsfile successfully validated.")) { "response=$response" }
    }

    @Test fun validate_notValidJenkinsfile_reportsError() {
        val response = jenkinsGateway.validate(INCORRECT_JENKINSFILE_CONTENT)

        assert(response.contains("Errors encountered validating Jenkinsfile")) { "response=$response" }
    }
}
