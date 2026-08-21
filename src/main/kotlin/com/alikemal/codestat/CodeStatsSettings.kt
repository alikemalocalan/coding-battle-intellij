package com.alikemal.codestat

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
        var apiToken: String = ""
        var updateIntervalSeconds: Long = CodeStatsConfig.DEFAULT_UPDATE_INTERVAL
    }

    private val myState = State()

    companion object {
        @JvmStatic
        fun getInstance(): CodeStatsSettings? {
            return ApplicationManager.getApplication()?.getService(CodeStatsSettings::class.java)
        }
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    fun getApiUrl(): String {
        return myState.apiUrl.ifBlank { CodeStatsConfig.DEFAULT_API_URL }
    }

    fun setApiUrl(apiUrl: String) {
        myState.apiUrl = apiUrl
    }

    fun getApiToken(): String = myState.apiToken

    fun setApiToken(apiToken: String) {
        myState.apiToken = apiToken
    }

    fun getUpdateIntervalSeconds(): Long {
        return if (myState.updateIntervalSeconds > 0) myState.updateIntervalSeconds else CodeStatsConfig.DEFAULT_UPDATE_INTERVAL
    }

    fun setUpdateIntervalSeconds(updateIntervalSeconds: Long) {
        myState.updateIntervalSeconds = updateIntervalSeconds
    }
}
