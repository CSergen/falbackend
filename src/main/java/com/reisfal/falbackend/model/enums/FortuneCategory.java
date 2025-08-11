package com.reisfal.falbackend.model.enums;

public enum FortuneCategory {
    KAHVE("Kahve Falı"),
    EL("El Falı"),
    TAROT("Tarot Falı"),
    ASTROLOJI("Astroloji Yorumu"),
    ASK("Aşk Falı"),
    GENEL("Genel Yorum Falı"); // yeni kategori

    private final String displayName;

    FortuneCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
