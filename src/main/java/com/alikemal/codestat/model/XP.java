package com.alikemal.codestat.model;

import org.apache.tapestry5.json.JSONObject;

public class XP {
    private String language;
    private int xp;

    public XP(String language, int xp) {
        this.language = language;
        this.xp = xp;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XP xp1 = (XP) o;

        if (xp != xp1.xp) return false;
        return language.equals(xp1.language);
    }

    @Override
    public int hashCode() {
        int result = language.hashCode();
        result = 31 * result + xp;
        return result;
    }

    public void increaseXP() {
        this.xp = ++xp;
    }


    // create by build json plugin
    public JSONObject toJson() {
        JSONObject jo = new JSONObject();
        jo.put("language", language);
        jo.put("xp", xp);
        return jo;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
