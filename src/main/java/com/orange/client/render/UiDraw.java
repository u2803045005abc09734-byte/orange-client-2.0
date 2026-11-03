package com.orange.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class UiDraw {
    private UiDraw() { }

    public static void rounded(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        for (int row = 0; row < height; row++) {
            double dy = row < r ? r - row - 0.5 : row >= height - r ? row - (height - r) + 0.5 : 0;
            int inset = 0;
            if (dy != 0) inset = Math.max(0, r - (int) Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy))));
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    public static void bordered(DrawContext context, int x, int y, int width, int height, int radius, int fill, int border) {
        rounded(context, x, y, width, height, radius, border);
        rounded(context, x + 1, y + 1, width - 2, height - 2, Math.max(0, radius - 1), fill);
    }

    public static void text(DrawContext context, String value, int x, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, Text.literal(value), x, y, color, false);
    }

    public static void shadowText(DrawContext context, String value, int x, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, Text.literal(value), x, y, color, true);
    }

    public static void centered(DrawContext context, String value, int centerX, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(value);
        text(context, value, centerX - textWidth / 2, y, color);
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void toggle(DrawContext context, int x, int y, boolean enabled, int accent) {
        int trackColor = enabled ? accent : 0xFF303342;
        rounded(context, x, y, 52, 28, 14, trackColor);
        int knobX = enabled ? x + 28 : x + 4;
        rounded(context, knobX, y + 4, 20, 20, 10, 0xFFF7F7FC);
    }
}
