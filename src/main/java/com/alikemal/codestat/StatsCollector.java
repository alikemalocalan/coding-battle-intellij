package com.alikemal.codestat;

import com.alikemal.codestat.ui.StatusBarIcon;
import com.alikemal.codestat.ui.TypingHandler;
import com.intellij.lang.Language;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.WindowManagerListener;
import org.fest.util.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class StatsCollector implements ApplicationComponent {
    /**
     * How long to wait before sending an update.
     */


    private Map<String, Integer> xps;
    private ScheduledFuture updateTimer;
    private ScheduledExecutorService executor;

    private Integer statusBarUniqueID;
    private Map<IdeFrame, StatusBarIcon> statusBarIcons;

    public StatsCollector() {
        statusBarIcons = new Hashtable<>();
        statusBarUniqueID = 0;

        /*
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("net.codestats.plugin.atom.intellij"));
        Config.VERSION = plugin.getVersion();
        */
    }

    @Override
    public void initComponent() {
        executor = Executors.newScheduledThreadPool(1);
        xps = Maps.newConcurrentHashMap();

        // Add the status bar icon to the statusbar of all windows when they are opened
        WindowManager.getInstance().addListener(new WindowManagerListener() {
            @Override
            public void frameCreated(IdeFrame ideFrame) {
                StatusBar statusBar = ideFrame.getStatusBar();

                if (statusBar == null) {
                    return;
                }

                StatusBarIcon statusBarIcon = new StatusBarIcon(statusBarUniqueID.toString(), statusBar);

                statusBar.addWidget(statusBarIcon);
                statusBarIcons.put(ideFrame, statusBarIcon);
                statusBarUniqueID += 1;
            }

            @Override
            public void beforeFrameReleased(IdeFrame ideFrame) {
                if (statusBarIcons.containsKey(ideFrame)) {
                    ideFrame.getStatusBar().removeWidget(statusBarIcons.get(ideFrame).ID());
                    statusBarIcons.remove(ideFrame);
                }
            }
        });

        // Set up keyhandler that will send us keypresses
        final EditorActionManager actionManager = EditorActionManager.getInstance();
        final TypedAction typedAction = actionManager.getTypedAction();
        final TypedActionHandler oldHandler = typedAction.getHandler();

        final TypingHandler handler = new TypingHandler();
        handler.setOldTypingHandler(oldHandler);
        handler.setStatsCollector(this);
        typedAction.setupHandler(handler);

    }

    @Override
    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "StatsCollector";
    }

    public void handleKeyEvent(Language language) {

        final String languageName = language.getDisplayName();

        if (xps.containsKey(languageName)) {
            xps.put(languageName, xps.get(languageName) + 1);
        } else {
            xps.put(languageName, 1);
        }
        // If timer is already running, cancel it to prevent updates when typing
        if (updateTimer != null && !updateTimer.isCancelled()) {
            updateTimer.cancel(false);
        }

        UpdateTask task = new UpdateTask(xps, statusBarIcons);
        updateTimer = executor.schedule(task, Config.UPDATE_TIMER, TimeUnit.SECONDS);
    }
}
