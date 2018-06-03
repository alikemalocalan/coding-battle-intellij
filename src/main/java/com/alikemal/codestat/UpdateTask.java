package com.alikemal.codestat;


import com.alikemal.codestat.model.XP;
import com.alikemal.codestat.model.XpResponse;
import com.alikemal.codestat.ui.StatusBarIcon;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Task that seds the update to the Code::Stats servers.
 */

public class UpdateTask implements Runnable {
    private Logger log = Logger.getInstance(UpdateTask.class);

    private Map<String, Integer> xps;
    private Map<IdeFrame, StatusBarIcon> statusBarIcons;

    public UpdateTask(Map<String, Integer> xps, Map<IdeFrame, StatusBarIcon> statusBarIcons) {
        this.xps = xps;
        this.statusBarIcons = statusBarIcons;
    }


    @Override
    public void run() {
        log.setLevel(Level.DEBUG);
        statusBarIcons.values().forEach(StatusBarIcon::setUpdating);

        List<XP> xpList = new ArrayList<>();

        xps.forEach((s, integer) -> xpList.add(new XP(s, integer)));

        final String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                .withZone(TimeZone.getDefault().toZoneId())
                .format(Instant.now());

        HttpResponse response = null;
        try {
            StringEntity entity = new StringEntity(new XpResponse(now, xpList).toString());

            HttpPost postMethod = new HttpPost(Config.DEFAULT_API_URL);
            postMethod.addHeader("Content-Type", "application/json");
            postMethod.addHeader("X-API-Token", Config.API_KEY_NAME);
            postMethod.addHeader("User-Agent", Config.CONFIG_PREFIX + Config.VERSION);
            postMethod.setEntity(entity);

            CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
            response = client.execute(postMethod);
            log.debug(Integer.toString(response.getStatusLine().getStatusCode()));
            log.debug(EntityUtils.toString(response.getEntity()));
            client.close();
        } catch (IOException e) {
            log.error(e);
        }

        if (response != null) {
            if (response.getStatusLine().getStatusCode() == 201) {
                xps.clear();
                statusBarIcons.values().forEach(StatusBarIcon::clear);
            } else if (response.getStatusLine().getStatusCode() == 403) {
                // The API key was wrong when updating
                for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                    statusBarIcon.setError("Unauthorized. Please check that your API key is valid.");
                }
            } else {
                for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                    statusBarIcon.setError("Unknown status code when updating: " + response.getStatusLine().getStatusCode());
                }
            }
        } else {
            for (StatusBarIcon statusBarIcon : statusBarIcons.values()) {
                statusBarIcon.setError("Connection timeout error");
                log.error("Connection timeout error");
            }
        }
    }
}
