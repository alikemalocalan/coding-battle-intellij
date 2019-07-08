package com.alikemal.codestat;

import com.alikemal.codestat.model.XP;
import com.alikemal.codestat.model.XpResponse;
import com.alikemal.codestat.ui.StatusBarIcon;
import com.alikemal.codestat.ui.TypingHandler;
import com.google.gson.Gson;
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
import okhttp3.*;
import org.fest.util.Maps;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alikemal.codestat.Config.API_KEY_NAME;
import static com.alikemal.codestat.Config.API_URL_NAME;

public class StatsCollector implements ApplicationComponent {
    /**
     * How long to wait before sending an update.
     */


    private Map<String, Integer> xps;
    private ScheduledFuture updateTimer;
    private ScheduledExecutorService executor;

    private String apiURL;
    private String apiKey;

    private final PropertiesComponent propertiesComponent;

    private Integer statusBarUniqueID;
    private String pluginVersion;
    private Map<IdeFrame, StatusBarIcon> statusBarIcons;

    public StatsCollector() {
        propertiesComponent = PropertiesComponent.getInstance();
        statusBarIcons = new Hashtable<>();
        statusBarUniqueID = 0;

        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.alikemal.code-stat-intellij"));
        if (plugin != null) {
            pluginVersion = plugin.getVersion();
        } else {
            pluginVersion = "unknown";
        }

    }

    @Override
    public void initComponent() {
        apiKey = propertiesComponent.getValue(API_KEY_NAME);
        apiURL = propertiesComponent.getValue(API_URL_NAME);

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

        // Don't collect data without an API key
        if (apiKey == null) return;

        final String languageName = language.getDisplayName();

        xps.put(languageName, xps.getOrDefault(languageName, 0) + 1);
        // If timer is already running, cancel it to prevent updates when typing
        if (updateTimer != null && !updateTimer.isCancelled())
            updateTimer.cancel(false);

        UpdateTask task = new UpdateTask(xps, statusBarIcons);
        updateTimer = executor.schedule(task, Config.UPDATE_TIMER, TimeUnit.SECONDS);
    }

    public static String makeRequest(Map<String, Integer> xps) {

        List<XP> xpList = xps.entrySet().stream()
                .map(enrty -> new XP(enrty.getKey(), enrty.getValue()))
                .collect(Collectors.toList());


        final String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                .withZone(TimeZone.getDefault().toZoneId())
                .format(Instant.now());
        return new Gson().toJson(new XpResponse(now, xpList));
    }


    public static Response doPostHttpCall(String url, String token, String json) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("X-API-Token", token)
                .header("User-Agent", Config.CONFIG_PREFIX + Config.VERSION)
                .post(RequestBody.create(JSON, json))
                .build();
        return client.newCall(request).execute();
    }
}
