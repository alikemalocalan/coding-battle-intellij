package com.alikemal.codestat.model;


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
}
