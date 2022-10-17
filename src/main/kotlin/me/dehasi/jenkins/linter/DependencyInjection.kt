package me.dehasi.jenkins.linter

import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object DependencyInjection {

    fun jenkinsGateway(client: HttpClient, baseUrl: String, credentials: Credentials): JenkinsGateway {
        return JenkinsGateway(client, baseUrl, credentials)
    }

    fun httpClient(
        timeoutSeconds: Long = 60,
        ignoreCertificate: Boolean = false
    ): HttpClient {

        val httpClientBuilder = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .version(HTTP_1_1)

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
