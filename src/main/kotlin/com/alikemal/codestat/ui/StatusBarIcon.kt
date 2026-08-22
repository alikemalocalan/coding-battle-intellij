package com.alikemal.codestat.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class StatusBarIcon(
    val project: Project
) : StatusBarWidget, TextPresentation {

    private var statusBar: StatusBar? = null
    private var text: String = "C::S"
    private var tooltip: String = "Code::Stats active"

    companion object {
        @JvmStatic
        fun notifyUpdating() {
            ApplicationManager.getApplication()?.messageBus?.syncPublisher(CodeStatsNotifier.TOPIC)?.onUpdating()
        }

        @JvmStatic
        fun notifySuccess() {
            ApplicationManager.getApplication()?.messageBus?.syncPublisher(CodeStatsNotifier.TOPIC)?.onSuccess()
        }

        @JvmStatic
        fun notifyError(errorMessage: String) {
            ApplicationManager.getApplication()?.messageBus?.syncPublisher(CodeStatsNotifier.TOPIC)?.onError(errorMessage)
        }
    }

    init {
        val app = ApplicationManager.getApplication()
        if (app != null) {
            val connection = app.messageBus.connect(this)
            connection.subscribe(CodeStatsNotifier.TOPIC, object : CodeStatsNotifier {
                override fun onUpdating() {
                    updateState("C::S…", "Updating Code::Stats…")
                }

                override fun onSuccess() {
                    updateState("C::S", "Code::Stats updated successfully")
                }

                override fun onError(errorMessage: String) {
                    updateState("C::S ERR!", "Code::Stats Error: $errorMessage")
                }
            })
        }
    }

    override fun ID(): String = CodeStatsWidgetFactory.WIDGET_ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        this.statusBar = null
    }

    override fun getText(): String = text

    override fun getAlignment(): Float = 0f

    override fun getTooltipText(): String = tooltip

    override fun getClickConsumer(): Consumer<MouseEvent>? = null

    private fun updateState(newText: String, newTooltip: String) {
        this.text = newText
        this.tooltip = newTooltip
        ApplicationManager.getApplication()?.invokeLater {
            statusBar?.updateWidget(ID())
        }
    }
}
