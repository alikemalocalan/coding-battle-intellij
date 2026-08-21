package com.alikemal.codestat.model

data class XP(
    val language: String,
    val xp: Int
) {
    fun toJson(): String {
        val escapedLang = language.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"language":"$escapedLang","xp":$xp}"""
    }
}
