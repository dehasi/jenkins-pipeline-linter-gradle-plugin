package me.dehasi.jenkins.linter

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers


class JenkinsGateway(private val client: HttpClient, private val baseURL: String) {

    fun validate(jenkinsfileContent: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseURL/pipeline-model-converter/validate"))
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .POST(multipart(jenkinsfileContent))
            .build()

        val response = client.send(request, BodyHandlers.ofString())

        return response.body()
    }

    private fun multipart(content: String) = BodyPublishers.ofString("""
        |--$boundary
        |Content-Disposition: form-data; name="$filename"
        |
        |$content
        |--$boundary--
        """.trimMargin()
        .replace("\n", "\r\n"))

    private companion object {
        const val filename = "jenkinsfile"
        const val boundary = "abcdefghijklmnopqrstuvwxyz1234567890"
    }
}
