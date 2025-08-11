package com.reisfal.falbackend.model.enums;

public enum FortuneCategory {
<<<<<<< HEAD
    ASK("Aşk / İlişkiler"),
    IS("İş / Kariyer / Para"),
    HEALTH("Sağlık"),
    SPIRIT("Ruhsal Durum / Enerji / Aura"),
    GENERAL("Genel / Hayat");
=======
    KAHVE("Kahve Falı"),
    EL("El Falı"),
    TAROT("Tarot Falı"),
    ASTROLOJI("Astroloji Yorumu"),
    ASK("Aşk Falı"),
    GENEL("Genel Yorum Falı"); // yeni kategori
>>>>>>> recover-2157

    private final String displayName;

    FortuneCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
