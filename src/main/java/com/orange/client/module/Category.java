package com.orange.client.module;

public enum Category {
    PVP("PvP"),
    VISUALS("Visuals"),
    HUD("HUD"),
    PLAYER("Player"),
    MISC("Misc");

    private final String title;

    Category(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
