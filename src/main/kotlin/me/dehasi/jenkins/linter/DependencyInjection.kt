package me.dehasi.jenkins.linter

import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_2
import java.time.Duration

object DependencyInjection {

    fun jenkinsGateway(client: HttpClient, baseUrl: String): JenkinsGateway {
        return JenkinsGateway(client, baseUrl)
    }

    fun httpClient(username: String, password: String, timeoutSeconds: Int = 60): HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds.toLong()))
            .authenticator(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password.toCharArray())
                }
            })
            .version(HTTP_2)
            .build()
}