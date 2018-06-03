package com.alikemal.codestat;

import com.alikemal.codestat.model.XP;
import com.alikemal.codestat.model.XpResponse;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class StatsCollectorTest {

    @Test
    public void testUpdateTask() {

        final String now = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
                .withZone(TimeZone.getDefault().toZoneId())
                .format(Instant.now());

        List<XP> xps = new ArrayList<>();
        xps.add(new XP("Java", 12));

        System.out.println(new XpResponse(now, xps).toString());
        System.out.println(new Gson().toJson(new XpResponse(now, xps)));

        HttpPost postMethod = new HttpPost(Config.DEFAULT_API_URL);
        postMethod.addHeader("Content-Type", "application/json");
        postMethod.addHeader("X-API-Token", Config.API_KEY_NAME);
        postMethod.addHeader("User-Agent", "code-stats-intellij/" + "unknown");
        CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        try {
            StringEntity entity = new StringEntity(new XpResponse(now, xps).toString());
            postMethod.setEntity(entity);
            HttpResponse response = client.execute(postMethod);
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
    }
}