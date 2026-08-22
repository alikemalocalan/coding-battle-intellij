package com.alikemal.codestat.ui

import com.intellij.util.messages.Topic

interface CodeStatsNotifier {

    companion object {
        @JvmField
        val TOPIC: Topic<CodeStatsNotifier> = Topic.create("CodeStatsNotifierTopic", CodeStatsNotifier::class.java)
    }

    fun onUpdating()
    fun onSuccess()
    fun onError(errorMessage: String)
}
