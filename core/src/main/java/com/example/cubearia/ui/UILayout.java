package com.example.cubearia.ui;

import com.example.cubearia.game.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum UILayout {
    INVENTORY(
        ui(SlotRegion.Type.INVENTORY, 0f, -0.12f),
        ui(SlotRegion.Type.HOTBAR, 0f, -0.15f)
    ),

    CRAFTING_TABLE(
        ui(SlotRegion.Type.INVENTORY, 0f, 40f),
        ui(SlotRegion.Type.CRAFTING, 200f, 60f)
    );

    private static final float SLOT_SIZE_PCT = 0.055f;
    private static final float SPACING_PCT = 0.01f;

    private final List<UIComponent> components;

    UILayout(UIComponent... components) {
        this.components = Arrays.asList(components);
    }

    public List<SlotRegion> create(float bgX, float bgY, float bgW, float bgH) {
        List<SlotRegion> result = new ArrayList<>();

        float scale = Settings.UISCALE * 2.13f;
        float slotSize = bgW * SLOT_SIZE_PCT * scale;
        float spacing = bgW * SPACING_PCT * scale;

        for (UIComponent comp : components) {
            float offsetX = bgW * comp.offsetXPct;
            float offsetY = bgH * comp.offsetYPct;
            switch (comp.type) {
                case INVENTORY:
                    result.addAll(drawInventory(bgX, bgY, bgW, bgH, slotSize, spacing, offsetX, offsetY));
                    break;
                case HOTBAR:
                    result.addAll(drawHotbar(bgX, bgY, bgW, bgH, slotSize, spacing, offsetX, offsetY));
                    break;
                case ARMOR:
                    result.addAll(drawArmor(bgX, bgY, bgW, bgH, slotSize, spacing, offsetX, offsetY));
                    break;
                case CRAFTING:
                    result.addAll(drawCrafting(bgX, bgY, bgW, bgH, slotSize, spacing, offsetX, offsetY));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private List<SlotRegion> drawInventory(float bgX, float bgY, float bgW, float bgH, float size, float spacing, float offsetX, float offsetY) {
        List<SlotRegion> result = new ArrayList<>();
        int rows = 3, cols = 9;
        float totalW = cols * size + (cols - 1) * spacing;
        float startX = bgX + (bgW - totalW) / 2f + offsetX;
        float startY = bgY + bgH * 0.2f + offsetY + (size + spacing);

        for (int row = 1; row <= rows; row++) {  // row läuft von 1 bis rows
            for (int col = 0; col < cols; col++) {
                float x = startX + col * (size + spacing);
                float y = startY + (rows - row) * (size + spacing);  // Umkehren der Reihenfolge

                int index = col + row * cols;
                result.add(new SlotRegion(index, x, y, size, size, SlotRegion.Type.INVENTORY));
            }
        }
        return result;
    }

    private List<SlotRegion> drawHotbar(float bgX, float bgY, float bgW, float bgH, float size, float spacing, float offsetX, float offsetY) {
        List<SlotRegion> result = new ArrayList<>();
        int cols = 9;
        int row = 0;
        float totalW = cols * size + (cols - 1) * spacing;
        float startX = bgX + (bgW - totalW) / 2f + offsetX;
        float startY = bgY + bgH * 0.2f + offsetY;

        for (int col = 0; col < cols; col++) {
            float x = startX + col * (size + spacing);
            float y = startY + row * (size + spacing);
            int index = col + row * cols;
            result.add(new SlotRegion(index, x, y, size, size, SlotRegion.Type.INVENTORY));
        }

        return result;
    }

    private List<SlotRegion> drawArmor(float bgX, float bgY, float bgW, float bgH, float size, float spacing, float offsetX, float offsetY) {
        List<SlotRegion> result = new ArrayList<>();
        float x = bgX + offsetX;
        float y = bgY + offsetY;
        for (int i = 0; i < 4; i++) {
            float slotY = y + i * (size + spacing);
            result.add(new SlotRegion(100 + i, x, slotY, size, size, SlotRegion.Type.ARMOR));
        }
        return result;
    }

    private List<SlotRegion> drawCrafting(float bgX, float bgY, float bgW, float bgH, float size, float spacing, float offsetX, float offsetY) {
        List<SlotRegion> result = new ArrayList<>();
        float x = bgX + offsetX;
        float y = bgY + offsetY;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                float sx = x + col * (size + spacing);
                float sy = y + row * (size + spacing);
                int index = 200 + col + row * 2;
                result.add(new SlotRegion(index, sx, sy, size, size, SlotRegion.Type.CRAFTING));
            }
        }

        // Output slot
        float outX = x + (size + spacing) * 2.5f;
        float outY = y + size / 2;
        result.add(new SlotRegion(204, outX, outY, size, size, SlotRegion.Type.OUTPUT));

        return result;
    }

    // Helfer für Enum-Definition
    private static UIComponent ui(SlotRegion.Type type, float offsetXPct, float offsetYPct) {
        return new UIComponent(type, offsetXPct, offsetYPct);
    }

    private static class UIComponent {
        public final SlotRegion.Type type;
        public final float offsetXPct;
        public final float offsetYPct;

        public UIComponent(SlotRegion.Type type, float offsetXPct, float offsetYPct) {
            this.type = type;
            this.offsetXPct = offsetXPct;
            this.offsetYPct = offsetYPct;
        }
    }
}
