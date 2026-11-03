package com.orange.client.render;

public final class Theme {
    private Theme() { }

    public static final int BACKDROP = 0xC9070910;
    public static final int WINDOW = 0xF20B0D14;
    public static final int PANEL = 0xF1111321;
    public static final int PANEL_ACTIVE = 0xF117182A;
    public static final int HOVER = 0xF2202139;
    public static final int TEXT = 0xFFF3F3FA;
    public static final int MUTED = 0xFFA5A8B7;
    public static final int DISABLED = 0xFF707486;
    public static final int BORDER = 0xFF292B3C;

    public static int accent(String theme) {
        return switch (theme == null ? "purple" : theme) {
            case "orange" -> 0xFFFF923D;
            case "blue" -> 0xFF65A8FF;
            default -> 0xFF8565F5;
        };
    }

    public static int accentSoft(String theme) {
        return switch (theme == null ? "purple" : theme) {
            case "orange" -> 0xFFFFB477;
            case "blue" -> 0xFF9BC6FF;
            default -> 0xFFA58CFF;
        };
    }
}
