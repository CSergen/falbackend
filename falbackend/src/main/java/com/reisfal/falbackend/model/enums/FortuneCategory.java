package com.reisfal.falbackend.model.enums;

public enum FortuneCategory {
    ASK("Aşk / İlişkiler"),
    IS("İş / Kariyer / Para"),
    HEALTH("Sağlık"),
    SPIRIT("Ruhsal Durum / Enerji / Aura"),
    GENERAL("Genel / Hayat");

    private final String displayName;

    FortuneCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
