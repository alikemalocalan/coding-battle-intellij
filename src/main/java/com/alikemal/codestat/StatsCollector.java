package com.alikemal.codestat;

import com.alikemal.codestat.ui.SettingsForm;
import com.alikemal.codestat.ui.StatusBarIcon;
import com.alikemal.codestat.ui.TypingHandler;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.WindowManagerListener;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class StatsCollector implements ApplicationComponent {
    /**
     * How long to wait before sending an update.
     */
    private final long UPDATE_TIMER = 10;

    // Is this bad? I have no idea. This is for the xps synchronization
    private final Object xps_lock = new Object();

    private final PropertiesComponent propertiesComponent;

    private Hashtable<String, Integer> xps;
    private ScheduledFuture updateTimer;
    private ScheduledExecutorService executor;

    private String apiURL;
    private String apiKey;

    private Integer statusBarUniqueID;
    private Hashtable<IdeFrame, StatusBarIcon> statusBarIcons;

    private String pluginVersion;

    public StatsCollector() {
        propertiesComponent = PropertiesComponent.getInstance();
        statusBarIcons = new Hashtable<>();
        statusBarUniqueID = 0;

        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("net.codestats.plugin.atom.intellij"));
        if (plugin != null) {
            pluginVersion = plugin.getVersion();
        } else {
            pluginVersion = "unknown";
        }
    }

    @Override
    public void initComponent() {
        apiKey = propertiesComponent.getValue(SettingsForm.API_KEY_NAME);
        apiURL = propertiesComponent.getValue(SettingsForm.API_URL_NAME);

        executor = Executors.newScheduledThreadPool(1);
        xps = new Hashtable<>();

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

        if (apiKey == null) {
            // Don't collect data without an API key
            return;
        }

        final String languageName = language.getDisplayName();

        synchronized (xps_lock) {
            if (xps.containsKey(languageName)) {
                xps.put(languageName, xps.get(languageName) + 1);
            } else {
                xps.put(languageName, 1);
            }
        }

        // If timer is already running, cancel it to prevent updates when typing
        if (updateTimer != null && !updateTimer.isCancelled()) {
            updateTimer.cancel(false);
        }

        UpdateTask task = new UpdateTask();
        task.setXpsLock(xps_lock);
        task.setXps(xps);
        task.setConfig(apiURL, apiKey);
        task.setStatusBarIcons(statusBarIcons);
        task.setVersion(pluginVersion);
        updateTimer = executor.schedule(task, UPDATE_TIMER, TimeUnit.SECONDS);
    }

    public void setApiConfig(final String apiURL, final String apiKey) {
        this.apiURL = apiURL;
        this.apiKey = apiKey;
    }
}
