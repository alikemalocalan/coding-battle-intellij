package com.alikemal.codestat.model

data class XpResponse(
    val codedAt: String,
    val xps: List<XP>
) {
    fun toJson(): String {
        val xpsJson = xps.joinToString(separator = ",", prefix = "[", postfix = "]") { it.toJson() }
        val escapedCodedAt = codedAt.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"coded_at":"$escapedCodedAt","xps":$xpsJson}"""
    }
}
