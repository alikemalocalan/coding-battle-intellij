package com.alikemal.codestat;


import com.alikemal.codestat.model.XP;
import com.alikemal.codestat.model.XpResponse;
import com.alikemal.codestat.ui.SettingsForm;
import com.alikemal.codestat.ui.StatusBarIcon;
import com.google.gson.Gson;
import com.intellij.openapi.wm.IdeFrame;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

/**
 * Task that sends the update to the Code::Stats servers.
 */

public class UpdateTask implements Runnable {

    private Hashtable<String, Integer> xps;
    private Object xpsLock;
    private String apiURL;
    private String apiKey;
    private Hashtable<IdeFrame, StatusBarIcon> statusBarIcons;
    private String version = "unknown";

    public UpdateTask() {
    }

    @Override
    public void run() {
        statusBarIcons.values().forEach(StatusBarIcon::setUpdating);

        try {
            List<XP> xpList = new ArrayList<>();

            xps.forEach((s, integer) -> xpList.add(new XP(s, integer)));

            final String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                    .withZone(TimeZone.getDefault().toZoneId())
                    .format(Instant.now());

            StringEntity entity = new StringEntity(new Gson().toJson(new XpResponse(now, xpList)));

            // Sometimes after some combination of user action, the API URL ends up as an empty string, guard against it
            if (apiURL == null || apiURL.equals("")) {
                apiURL = SettingsForm.DEFAULT_API_URL;
            }

            final String API_TOKEN = apiKey;

            HttpPost postMethod = new HttpPost(apiURL);
            postMethod.addHeader("Content-Type", "application/json");
            postMethod.addHeader("X-API-Token", API_TOKEN);
            postMethod.addHeader("User-Agent", "code-stats-intellij/" + version);
            postMethod.setEntity(entity);

            CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
            HttpResponse response = null;
            try {
                response = client.execute(postMethod);
                System.out.println(response.getStatusLine().getStatusCode());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (response.getStatusLine().getStatusCode() == 201) {
                synchronized (xpsLock) {
                    xps.clear();
                }

                statusBarIcons.values().forEach(StatusBarIcon::clear);
            } else if (response.getStatusLine().getStatusCode() == 403) {
                // The API key was wrong when updating
                for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                    statusBarIcon.setError("Unauthorized. Please check that your API key is valid.");
                }
            } else {
                for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                    statusBarIcon.setError("Unknown status code when updating: " + String.valueOf(response.getStatusLine().getStatusCode()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                statusBarIcon.setError(e.toString());
            }
        }
    }

    public void setXps(Hashtable<String, Integer> xps) {
        this.xps = xps;
    }

    public void setXpsLock(final Object xpsLock) {
        this.xpsLock = xpsLock;
    }

    public void setConfig(final String apiURL, final String apiKey) {
        this.apiURL = apiURL;
        this.apiKey = apiKey;
    }

    public void setStatusBarIcons(Hashtable<IdeFrame, StatusBarIcon> statusBarIcons) {
        this.statusBarIcons = statusBarIcons;
    }


    public void setVersion(String version) {
        this.version = version;
    }
}
