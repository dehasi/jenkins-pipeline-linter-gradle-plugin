package me.dehasi.jenkins.linter

import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_2
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object DependencyInjection {

    fun jenkinsGateway(client: HttpClient, baseUrl: String): JenkinsGateway {
        return JenkinsGateway(client, baseUrl)
    }

    fun httpClient(
        username: String,
        password: String,
        timeoutSeconds: Long = 60,
        ignoreCertificate: Boolean = false
    ): HttpClient {

        val httpClientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .authenticator(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password.toCharArray())
                }
            })
            .version(HTTP_2)

        if (ignoreCertificate) httpClientBuilder.sslContext(insecureContext())
        return httpClientBuilder.build()
    }

    private fun insecureContext(): SSLContext? {
        val noopTrustManager = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(xcs: Array<X509Certificate?>?, string: String?) {}
                override fun checkServerTrusted(xcs: Array<X509Certificate?>?, string: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }
            }
        )
        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, noopTrustManager, null)
            return sc
        } catch (e: KeyManagementException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}
