package com.alikemal.codestat

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CodeStatsStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        CodeStatsService.getInstance()
    }
}
