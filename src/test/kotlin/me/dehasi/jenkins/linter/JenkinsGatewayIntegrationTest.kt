package me.dehasi.jenkins.linter

import org.junit.jupiter.api.Test
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_2
import java.time.Duration

internal class JenkinsGatewayIntegrationTest {

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .authenticator(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("admin", "admin".toCharArray())
            }
        })
        .version(HTTP_2)
        .build()
    private val jenkinsGateway = JenkinsGateway(client, "http://localhost:8080")

    @Test fun validate_validJenkinsfile_reportsSuccess() {
        val jenkinsFile = """
        pipeline {
          agent any
            stages {
              stage ('Initialize') {
              steps {
                echo 'Placeholder.'
              }
            }
          }
        }""".trimIndent()

        val response = jenkinsGateway.validate(jenkinsFile)

        assert(response.contains("Jenkinsfile successfully validated.")) { "response=$response" }
    }

    @Test fun validate_notValidJenkinsfile_reportsError() {
        val jenkinsFile = """
        pipeline {
          agent 
            stages {
              stage ('Initialize') {
              steps {
                echo 'Placeholder.'
              }
            }
          }
        }""".trimIndent()

        val response = jenkinsGateway.validate(jenkinsFile)

        assert(response.contains("Errors encountered validating Jenkinsfile")) { "response=$response" }
    }
}
