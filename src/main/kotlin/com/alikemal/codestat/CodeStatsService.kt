package com.alikemal.codestat

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CodeStatsService : Disposable {

    private val xps = ConcurrentHashMap<String, Int>()
    private val executor: ScheduledExecutorService =
        AppExecutorUtil.createBoundedScheduledExecutorService("CodeStatsExecutor", 1)
    private var updateTimer: ScheduledFuture<*>? = null

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

                val language = getLanguageName(file)
                handleKeyEvent(language, addedLength)
            }
        }, this)
    }

    fun handleKeyEvent(languageName: String, count: Int) {
        xps.merge(languageName, count) { a, b -> a + b }

        val settings = CodeStatsSettings.getInstance()
        val interval = settings?.getUpdateIntervalSeconds() ?: CodeStatsConfig.DEFAULT_UPDATE_INTERVAL

        synchronized(this) {
            updateTimer?.takeIf { !it.isCancelled }?.cancel(false)
            val task = UpdateTask(xps)
            updateTimer = executor.schedule(task, interval, TimeUnit.SECONDS)
        }
    }

    private fun getLanguageName(file: VirtualFile): String {
        val name = file.fileType.name
        if (name.equals("Unknown", ignoreCase = true) || name.equals("PLAIN_TEXT", ignoreCase = true)) {
            val ext = file.extension
            return if (!ext.isNullOrBlank()) ext else "Plain text"
        }
        return name
    }

    override fun dispose() {
        executor.shutdownNow()
    }
}
