package com.somefrills.features.misc

import java.net.CookieManager
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

object SteganosProxyClient {

    private const val PROXY_ENDPOINT =
        "https://proxy-de.steganos.com/includes/process.php?action=update"

    private val cookieManager = CookieManager()

    private val client = HttpClient.newBuilder()
        .cookieHandler(cookieManager)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private fun fetch(targetUrl: String): String? {
        return try {
            val encodedUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8)

            val formBody =
                "u=$encodedUrl" +
                        "&wp_location=" + URLEncoder.encode(PROXY_ENDPOINT, StandardCharsets.UTF_8)

            val request = HttpRequest.newBuilder()
                .uri(URI.create(PROXY_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build()

            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun lowestBin(): String? =
        fetch("https://scamscreener.creepans.net/api/v2/lowestbin")

    fun bazaar(): String? =
        fetch("https://scamscreener.creepans.net/api/v1/bazaar")
}