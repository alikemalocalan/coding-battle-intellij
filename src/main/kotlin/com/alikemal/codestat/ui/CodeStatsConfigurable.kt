package com.alikemal.codestat.ui

import com.alikemal.codestat.CodeStatsSettings
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class CodeStatsConfigurable : Configurable {

    private var apiUrlField: JBTextField? = null
    private var apiTokenField: JBPasswordField? = null
    private var updateIntervalField: JBTextField? = null

    override fun getDisplayName(): String = "Code::Stats"

    override fun createComponent(): JComponent {
        val apiUrl = JBTextField()
        val apiToken = JBPasswordField()
        val updateInterval = JBTextField()

        this.apiUrlField = apiUrl
        this.apiTokenField = apiToken
        this.updateIntervalField = updateInterval

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("API URL:"), apiUrl, 1, false)
            .addLabeledComponent(JBLabel("API Token:"), apiToken, 1, false)
            .addLabeledComponent(JBLabel("Pulse Interval (seconds):"), updateInterval, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = CodeStatsSettings.getInstance() ?: return false

        val currentUrl = apiUrlField?.text ?: ""
        val currentToken = String(apiTokenField?.password ?: charArrayOf())
        val currentInterval = updateIntervalField?.text ?: ""

        return currentUrl != settings.getApiUrl() ||
                currentToken != settings.getApiToken() ||
                currentInterval != settings.getUpdateIntervalSeconds().toString()
    }

    override fun apply() {
        val settings = CodeStatsSettings.getInstance() ?: return

        val url = apiUrlField?.text?.trim() ?: ""
        val token = String(apiTokenField?.password ?: charArrayOf()).trim()
        val intervalText = updateIntervalField?.text?.trim() ?: ""

        settings.setApiUrl(url)
        settings.setApiToken(token)

        try {
            val interval = intervalText.toLong()
            if (interval <= 0) {
                throw NumberFormatException()
            }
            settings.setUpdateIntervalSeconds(interval)
        } catch (e: NumberFormatException) {
            throw ConfigurationException("Pulse interval must be a positive integer.")
        }
    }

    override fun reset() {
        val settings = CodeStatsSettings.getInstance() ?: return

        apiUrlField?.text = settings.getApiUrl()
        apiTokenField?.text = settings.getApiToken()
        updateIntervalField?.text = settings.getUpdateIntervalSeconds().toString()
    }

    override fun disposeUIResources() {
        apiUrlField = null
        apiTokenField = null
        updateIntervalField = null
    }
}
