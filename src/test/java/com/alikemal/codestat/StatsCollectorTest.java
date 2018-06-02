package com.alikemal.codestat;

import com.alikemal.codestat.model.XP;
import com.alikemal.codestat.model.XpResponse;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatsCollectorTest {

    @Test
    public void testHttpRequest() {

        HttpPost postMethod = new HttpPost("https://codestats.net/");
                postMethod.addHeader("Content-Type", "application/json");
                //.addHeader("X-API-Token", API_TOKEN)
                postMethod.addHeader("User-Agent", "code-stats-intellij/" + "unknown");
        CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        try {
            HttpResponse response = client.execute(postMethod);
            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void inputJson(){
        String now = "123";

        List<XP> xps = new ArrayList<>();
        xps.add(new XP("jv", 12));
        xps.add(new XP("js", 1));

        System.out.println(new Gson().toJson(new XpResponse(now,xps)));
    }
}