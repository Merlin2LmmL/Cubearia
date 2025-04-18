package com.example.cubearia.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.Inventory;
import com.example.cubearia.game.ItemStack;

public class InventoryUI {
    private final Inventory inventory;
    private final Texture slotBackground;
    private final BitmapFont font;
    private final float slotSize = 48f;

    public InventoryUI(Inventory inventory, Texture slotBackground, BitmapFont font) {
        this.inventory = inventory;
        this.slotBackground = slotBackground;
        this.font = font;
    }

    public void render(SpriteBatch batch, OrthographicCamera uiCam, int selectedSlot) {
        float vw = uiCam.viewportWidth;   // in UI‑Units = Fensterpixel
        float vh = uiCam.viewportHeight;

        // 1) Slot-Größe = 6% der Fensterbreite
        float slotSizePx = vw * 0.06f;
        // 2) Padding = 10% von slotSize
        float paddingPx  = slotSizePx * 0.1f;

        // 3) gesamte Breite aller Slots
        float totalWidth = inventory.getSize() * slotSizePx
            + (inventory.getSize() - 1) * paddingPx;
        // 4) zentriert horizontal
        float startX = (vw - totalWidth) / 2f;
        // 5) 5% über dem unteren Rand
        float startY = vh * 0.05f;

        for (int i = 0; i < inventory.getSize(); i++) {
            float x = startX + i * (slotSizePx + paddingPx);
            float y = startY;

            // Hervorheben des ausgewählten Slots
            if (i == selectedSlot) {
                batch.setColor(0.6f, 0.6f, 0.6f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }
            batch.draw(slotBackground, x, y, slotSizePx, slotSizePx);
            batch.setColor(1f, 1f, 1f, 1f);

            ItemStack stack = inventory.getSlot(i);
            if (stack != null) {
                // Icon mit 80% des Slot‑Innenraums
                float iconSize = slotSizePx * 0.8f;
                float iconOffset = slotSizePx * 0.1f;
                batch.draw(stack.getItem().getIcon(),
                    x + iconOffset, y + iconOffset,
                    iconSize, iconSize);
                font.draw(batch,
                    String.valueOf(stack.getAmount()),
                    x + slotSizePx * 0.75f,
                    y + slotSizePx * 0.25f);
            }
        }
    }
}
