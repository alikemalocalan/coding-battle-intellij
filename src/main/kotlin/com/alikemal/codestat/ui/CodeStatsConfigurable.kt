package com.alikemal.codestat.ui

import com.alikemal.codestat.CodeStatsSettings
import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class CodeStatsConfigurable : Configurable {

    private val settings: CodeStatsSettings?
        get() = CodeStatsSettings.getInstance()

    private var apiUrl: String = ""
    private var apiToken: String = ""
    private var updateIntervalText: String = "10"

    private val panelComponent by lazy {
        panel {
            group("Code::Stats Settings") {
                row("API URL:") {
                    textField()
                        .bindText(::apiUrl)
                        .columns(COLUMNS_MEDIUM)
                }
                row("API Token:") {
                    passwordField()
                        .bindText(::apiToken)
                        .columns(COLUMNS_MEDIUM)
                }
                row("Pulse Interval (seconds):") {
                    textField()
                        .bindText(::updateIntervalText)
                        .columns(COLUMNS_MEDIUM)
                }
            }
        }
    }

    override fun getDisplayName(): String = "Code::Stats"

    override fun createComponent(): JComponent {
        reset()
        return panelComponent
    }

    override fun isModified(): Boolean {
        return panelComponent.isModified()
    }

    override fun apply() {
        panelComponent.apply()

        val s = settings ?: return
        s.setApiUrl(apiUrl.trim())
        s.setApiToken(apiToken.trim())

        val interval = updateIntervalText.trim().toLongOrNull()
        if (interval != null && interval > 0) {
            s.setUpdateIntervalSeconds(interval)
        }
    }

    override fun reset() {
        val s = settings
        if (s != null) {
            apiUrl = s.getApiUrl()
            apiToken = s.getApiToken()
            updateIntervalText = s.getUpdateIntervalSeconds().toString()
            panelComponent.reset()

            s.loadTokenFromStorageAsync {
                val token = s.getApiToken()
                if (apiToken != token) {
                    apiToken = token
                    panelComponent.reset()
                }
            }
        }
    }
}
