package com.alikemal.codestat.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import java.util.concurrent.ConcurrentHashMap

class StatusBarIcon(
    val project: Project
) : StatusBarWidget, TextPresentation {

    private var statusBar: StatusBar? = null
    private var text: String = "C::S"
    private var tooltip: String = "Code::Stats active"

    companion object {
        private val ACTIVE_WIDGETS = ConcurrentHashMap.newKeySet<StatusBarIcon>()

        @JvmStatic
        fun setUpdatingAll() {
            ApplicationManager.getApplication().invokeLater {
                ACTIVE_WIDGETS.forEach { icon ->
                    icon.updateState("C::S…", "Updating Code::Stats…")
                }
            }
        }

        @JvmStatic
        fun setSuccessAll() {
            ApplicationManager.getApplication().invokeLater {
                ACTIVE_WIDGETS.forEach { icon ->
                    icon.updateState("C::S", "Code::Stats updated successfully")
                }
            }
        }

        @JvmStatic
        fun setErrorAll(errorMsg: String) {
            ApplicationManager.getApplication().invokeLater {
                ACTIVE_WIDGETS.forEach { icon ->
                    icon.updateState("C::S ERR!", "Code::Stats Error: $errorMsg")
                }
            }
        }
    }

    init {
        ACTIVE_WIDGETS.add(this)
    }

    override fun ID(): String = CodeStatsWidgetFactory.WIDGET_ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        ACTIVE_WIDGETS.remove(this)
        this.statusBar = null
    }

    override fun getText(): String = text

    override fun getAlignment(): Float = 0f

    override fun getTooltipText(): String = tooltip

    override fun getClickConsumer(): Consumer<MouseEvent>? = null

    fun updateState(newText: String, newTooltip: String) {
        this.text = newText
        this.tooltip = newTooltip
        statusBar?.updateWidget(ID())
    }
}
