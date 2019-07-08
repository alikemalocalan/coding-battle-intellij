package com.alikemal.codestat;


import com.alikemal.codestat.ui.StatusBarIcon;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import okhttp3.Response;
import org.apache.log4j.Level;

import java.io.IOException;
import java.util.Map;

import static com.alikemal.codestat.Config.API_KEY_NAME;
import static com.alikemal.codestat.Config.API_URL_NAME;

/**
 * Task that seds the update to the Code::Stats servers.
 */

public class UpdateTask implements Runnable {
    private Logger log = Logger.getInstance(this.getClass());

    private Map<String, Integer> xps;
    private Map<IdeFrame, StatusBarIcon> statusBarIcons;
    private final PropertiesComponent propertiesComponent;

    private String apiURL;
    private String apiKey;

    public UpdateTask(Map<String, Integer> xps, Map<IdeFrame, StatusBarIcon> statusBarIcons) {
        propertiesComponent = PropertiesComponent.getInstance();
        this.xps = xps;
        this.statusBarIcons = statusBarIcons;
    }


    @Override
    public void run() {
        log.setLevel(Level.DEBUG);
        apiKey = propertiesComponent.getValue(API_KEY_NAME);
        apiURL = propertiesComponent.getValue(API_URL_NAME);

        statusBarIcons.values().forEach(StatusBarIcon::setUpdating);

        Response response=null;
        try {
            response=StatsCollector.doPostHttpCall(apiURL,apiKey,StatsCollector.makeRequest(xps));
        } catch (IOException e) {
            log.error("Request error",e);
        }


        if (response != null) {
            int status = response.code();
            log.debug(response.body().toString());
            if (status == 201) {
                xps.clear();
                statusBarIcons.values().forEach(StatusBarIcon::clear);
            } else // The API key was wrong when updating
                if (status == 403)
                    statusBarIcons.values().forEach(statusBarIcon ->statusBarIcon.setError("Unauthorized. Please check that your API key is valid."));
                else statusBarIcons.values().forEach(statusBarIcon ->statusBarIcon.setError("Unknown status code when updating: " + status));

        } else statusBarIcons.values().forEach(statusBarIcon -> statusBarIcon.setError("Connection timeout error"));
    }
}
