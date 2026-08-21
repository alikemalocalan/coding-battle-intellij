package com.alikemal.codestat

import com.alikemal.codestat.api.CodeStatsApiClient
import com.alikemal.codestat.api.PulseResult
import com.alikemal.codestat.ui.StatusBarIcon
import java.util.concurrent.ConcurrentMap

class UpdateTask(
    private val xps: ConcurrentMap<String, Int>,
    private val apiClient: CodeStatsApiClient = CodeStatsApiClient()
) : Runnable {

    override fun run() {
        if (xps.isEmpty()) return

        val settings = CodeStatsSettings.getInstance()
        val apiToken = settings?.getApiToken() ?: ""
        val apiUrl = settings?.getApiUrl() ?: CodeStatsConfig.DEFAULT_API_URL

        if (apiToken.isBlank()) {
            StatusBarIcon.setErrorAll("API key not set in Settings | Tools | Code::Stats")
            return
        }

        StatusBarIcon.setUpdatingAll()

        // Take a snapshot copy of current XP values to send
        val snapshot = HashMap(xps)
        when (val result = apiClient.sendPulse(apiUrl, apiToken, snapshot)) {
            is PulseResult.Success -> {
                // Deduct updated XP counts safely
                snapshot.forEach { (lang, count) ->
                    xps.computeIfPresent(lang) { _, current ->
                        val remaining = current - count
                        if (remaining <= 0) null else remaining
                    }
                }
                StatusBarIcon.setSuccessAll()
            }
            is PulseResult.Unauthorized -> {
                StatusBarIcon.setErrorAll(result.message)
            }
            is PulseResult.HttpError -> {
                StatusBarIcon.setErrorAll(result.message)
            }
            is PulseResult.ConnectionError -> {
                StatusBarIcon.setErrorAll("Connection error: ${result.exception.message}")
            }
        }
    }
}
