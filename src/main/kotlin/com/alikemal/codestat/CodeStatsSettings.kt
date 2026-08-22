package com.alikemal.codestat

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "CodeStatsSettings",
    storages = [Storage("codeStatsSettings.xml")]
)
class CodeStatsSettings : PersistentStateComponent<CodeStatsSettings.State> {

    class State {
        var apiUrl: String = CodeStatsConfig.DEFAULT_API_URL
        var updateIntervalSeconds: Long = CodeStatsConfig.DEFAULT_UPDATE_INTERVAL
    }

    private val myState = State()

    @Volatile
    private var cachedApiToken: String = ""

    companion object {
        private val CREDENTIAL_ATTRIBUTES = CredentialAttributes(
            "CodeStatsSettings",
            "CodeStatsApiToken"
        )

        @JvmStatic
        fun getInstance(): CodeStatsSettings? {
            return ApplicationManager.getApplication()?.getService(CodeStatsSettings::class.java)
        }
    }

    init {
        loadTokenFromStorageAsync()
    }

    fun loadTokenFromStorageAsync(onComplete: Runnable? = null) {
        ApplicationManager.getApplication()?.executeOnPooledThread {
            val token = PasswordSafe.instance.getPassword(CREDENTIAL_ATTRIBUTES) ?: ""
            cachedApiToken = token
            if (onComplete != null) {
                ApplicationManager.getApplication()?.invokeLater(onComplete)
            }
        }
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
        loadTokenFromStorageAsync()
    }

    fun getApiUrl(): String {
        return myState.apiUrl.ifBlank { CodeStatsConfig.DEFAULT_API_URL }
    }

    fun setApiUrl(apiUrl: String) {
        myState.apiUrl = apiUrl
    }

    fun getApiToken(): String {
        return cachedApiToken
    }

    fun setApiToken(apiToken: String) {
        val trimmed = apiToken.trim()
        cachedApiToken = trimmed
        ApplicationManager.getApplication()?.executeOnPooledThread {
            PasswordSafe.instance.setPassword(CREDENTIAL_ATTRIBUTES, trimmed.ifBlank { null })
        }
    }

    fun getUpdateIntervalSeconds(): Long {
        return if (myState.updateIntervalSeconds > 0) myState.updateIntervalSeconds else CodeStatsConfig.DEFAULT_UPDATE_INTERVAL
    }

    fun setUpdateIntervalSeconds(updateIntervalSeconds: Long) {
        myState.updateIntervalSeconds = updateIntervalSeconds
    }
}
