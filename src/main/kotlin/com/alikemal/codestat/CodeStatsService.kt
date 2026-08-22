package com.alikemal.codestat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Service(Service.Level.APP)
class CodeStatsService : Disposable {

    private val xps = ConcurrentHashMap<String, Int>()
    private val languageCache = ConcurrentHashMap<FileType, String>()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var debounceJob: Job? = null

    companion object {
        @JvmStatic
        fun getInstance(): CodeStatsService {
            return ApplicationManager.getApplication().getService(CodeStatsService::class.java)
        }
    }

    init {
        initListener()
    }

    private fun initListener() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val addedLength = event.newFragment.length
                if (addedLength <= 0) return

                val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
                if (!file.isInLocalFileSystem) return

                val language = getLanguageNameCached(file)
                handleKeyEvent(language, addedLength)
            }
        }, this)
    }

    fun handleKeyEvent(languageName: String, count: Int) {
        xps.merge(languageName, count) { a, b -> a + b }

        val settings = CodeStatsSettings.getInstance()
        val intervalSeconds = settings?.getUpdateIntervalSeconds() ?: CodeStatsConfig.DEFAULT_UPDATE_INTERVAL

        synchronized(this) {
            debounceJob?.cancel()
            debounceJob = serviceScope.launch {
                delay((intervalSeconds * 1000L).milliseconds)
                UpdateTask(xps).run()
            }
        }
    }

    private fun getLanguageNameCached(file: VirtualFile): String {
        val fileType = file.fileType
        return languageCache.computeIfAbsent(fileType) { ft ->
            val name = ft.name
            if (name.equals("Unknown", ignoreCase = true) || name.equals("PLAIN_TEXT", ignoreCase = true)) {
                val ext = file.extension
                if (!ext.isNullOrBlank()) ext else "Plain text"
            } else {
                name
            }
        }
    }

    override fun dispose() {
        debounceJob?.cancel()
    }
}
