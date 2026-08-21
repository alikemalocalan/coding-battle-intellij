package com.alikemal.codestat.api

import com.alikemal.codestat.CodeStatsConfig
import com.alikemal.codestat.model.XP
import com.alikemal.codestat.model.XpResponse
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed interface PulseResult {
    data object Success : PulseResult
    data class Unauthorized(val message: String) : PulseResult
    data class HttpError(val code: Int, val message: String) : PulseResult
    data class ConnectionError(val exception: Throwable) : PulseResult
}

class CodeStatsApiClient {

    companion object {
        private val LOG = Logger.getInstance(CodeStatsApiClient::class.java)

        /**
         * Singleton HttpClient instance reused across all pulse updates.
         */
        val HTTP_CLIENT: HttpClient by lazy {
            HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        }

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.systemDefault())

        const val USER_AGENT: String = "${CodeStatsConfig.CONFIG_PREFIX}${CodeStatsConfig.VERSION}"
    }

    fun sendPulse(apiUrl: String, apiToken: String, xpMap: Map<String, Int>): PulseResult {
        if (xpMap.isEmpty()) {
            return PulseResult.Success
        }

        if (apiToken.isBlank()) {
            return PulseResult.Unauthorized("API key not set in Settings | Tools | Code::Stats")
        }

        val xpList = xpMap.map { (lang, count) -> XP(lang, count) }
        val nowFormatted = DATE_FORMATTER.format(Instant.now())
        val jsonPayload = XpResponse(nowFormatted, xpList).toJson()

        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("X-API-Token", apiToken)
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            val response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
            val statusCode = response.statusCode()
            LOG.debug("Code::Stats pulse status code: $statusCode")

            when (statusCode) {
                201 -> PulseResult.Success
                403 -> PulseResult.Unauthorized("Unauthorized. Please check your API key in Settings.")
                else -> PulseResult.HttpError(statusCode, "HTTP error status: $statusCode")
            }
        } catch (e: Exception) {
            LOG.warn("Failed to send Code::Stats update pulse", e)
            PulseResult.ConnectionError(e)
        }
    }
}
