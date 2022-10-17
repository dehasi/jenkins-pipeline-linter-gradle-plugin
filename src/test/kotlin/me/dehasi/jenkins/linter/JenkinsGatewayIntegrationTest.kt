package me.dehasi.jenkins.linter

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import me.dehasi.jenkins.linter.DependencyInjection.httpClient
import me.dehasi.jenkins.linter.DependencyInjection.jenkinsGateway
import me.dehasi.jenkins.linter.Jenkinsfiles.CORRECT_JENKINSFILE_CONTENT
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
internal class JenkinsGatewayIntegrationTest {

    companion object {
        const val HTTP_PORT = 8089
        const val HTTPS_PORT = 8889
        const val USERNAME = "admin"
        const val PASSWORD = "admin"
    }

    private lateinit var jenkinsGateway: JenkinsGateway

    @BeforeEach fun createJenkinsGateway() {
        jenkinsGateway = jenkinsGateway(httpClient(ignoreCertificate = true),
            "http://localhost:$HTTP_PORT", Credentials(USERNAME, PASSWORD))
    }

    private lateinit var wireMockServer: WireMockServer
    @BeforeEach fun createWiremock() {
        wireMockServer = WireMockServer(options().port(HTTP_PORT).httpsPort(HTTPS_PORT)
            .caKeystorePath("jenkins.jks")
            .caKeystorePassword("password"))
        wireMockServer.start()
    }

    @AfterEach fun stopWiremock() {
        wireMockServer.stop()
    }

    @Test fun validate_sendsBasicAuthorisation() {
        wireMockServer.stubFor(
            post("/pipeline-model-converter/validate")
                .withRequestBody(containing("Content-Disposition: form-data; name=\"jenkinsfile\""))
                .willReturn(ok()));

        jenkinsGateway.validate(CORRECT_JENKINSFILE_CONTENT)

        wireMockServer.verify(postRequestedFor(urlEqualTo("/pipeline-model-converter/validate"))
            .withBasicAuth(BasicCredentials(USERNAME, PASSWORD)));
    }

    @Test fun validate_sendsJenkinsfileAdMultipart() {
        wireMockServer.stubFor(
            post("/pipeline-model-converter/validate")
                .withHeader("Content-Type", containing("multipart/form-data; boundary="))
                .withRequestBody(containing("Content-Disposition: form-data; name=\"jenkinsfile\""))
                .withRequestBody(containing("""
                    |--abcdefghijklmnopqrstuvwxyz1234567890
                    |Content-Disposition: form-data; name="jenkinsfile"
                    |
                    |pipeline {
                    |  agent any
                    |    stages {
                    |      stage ('Initialize') {
                    |      steps {
                    |        echo 'Placeholder.'
                    |      }
                    |    }
                    |  }
                    |}
                    |
                    |--abcdefghijklmnopqrstuvwxyz1234567890--
                """.trimMargin().replace("\n", "\r\n")))
                .willReturn(ok()));

        jenkinsGateway.validate(CORRECT_JENKINSFILE_CONTENT)

        wireMockServer.verify(postRequestedFor(urlEqualTo("/pipeline-model-converter/validate")))
    }

    @Test fun validate_errorResponse_throwsException() {
        wireMockServer.stubFor(
            post("/pipeline-model-converter/validate")
                .withHeader("Content-Type", containing("multipart/form-data; boundary="))
                .withRequestBody(containing("Content-Disposition: form-data; name=\"jenkinsfile\""))
                .withRequestBody(containing("pipeline"))
                .willReturn(status(400).withBody("error message")));

        assertThrows(IllegalStateException::class.java, {
            jenkinsGateway.validate(CORRECT_JENKINSFILE_CONTENT)
        }, "Throws exception on error code response")
    }
}
