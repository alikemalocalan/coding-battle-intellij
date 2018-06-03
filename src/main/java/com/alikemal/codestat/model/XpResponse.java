package com.alikemal.codestat.model;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import java.util.List;

public class XpResponse {

    private String coded_at;
    private List<XP> xps;

    public XpResponse() {
    }

    public XpResponse(String coded_at, List<XP> xps) {
        this.coded_at = coded_at;
        this.xps = xps;
    }

    public String getCoded_at() {
        return coded_at;
    }

    public void setCoded_at(String coded_at) {
        this.coded_at = coded_at;
    }

    public List<XP> getXps() {
        return xps;
    }

    public void setXps(List<XP> xps) {
        this.xps = xps;
    }


    // create by build json plugin
    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        jo.put("coded_at", coded_at);
        JSONArray jArray = new JSONArray();
        for (XP x : xps) {
            jArray.put(x.toJson());
        }
        jo.put("xps", jArray);
        return jo;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
