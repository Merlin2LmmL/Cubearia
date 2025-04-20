package com.example.cubearia.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.Settings;

public class InventoryUI {
    private final Inventory inventory;
    private final Texture slotBackground;
    private final Texture inventoryBackground;
    private final BitmapFont font;
    private final float uiSize = Settings.UISCALE;

    public InventoryUI(Inventory inventory, Texture slotBackground, Texture inventoryBackground, BitmapFont font) {
        this.inventory = inventory;
        this.slotBackground = slotBackground;
        this.inventoryBackground = inventoryBackground;
        this.font = font;
    }

    /**
     * Rendert das Inventar.
     * @param batch           SpriteBatch
     * @param uiCam           UI-Kamera
     * @param selectedSlotIdx Index des markierten Slots (0 bis totalSlots-1)
     * @param inventoryOpened true: ganzes Inventar auf Hintergrund, false: nur erste Reihe
     */
    public void render(SpriteBatch batch, OrthographicCamera uiCam, int selectedSlotIdx, boolean inventoryOpened) {
        float vw = uiCam.viewportWidth;
        float vh = uiCam.viewportHeight;

        // Basis-Slot-Größe in UI‑Units
        float slotSizePx = vw * 0.06f * uiSize;

        if (!inventoryOpened) {
            slotSizePx = vw * 0.06f * uiSize * 1.5f;
            // Nur die erste Reihe (Hotbar)
            float paddingPx  = slotSizePx * 0.1f;
            int cols = inventory.getColumns();

            float totalWidth = cols * slotSizePx + (cols - 1) * paddingPx;
            float startX     = (vw - totalWidth) / 2f;
            float startY     = vh * 0.05f;

            for (int col = 0; col < cols; col++) {
                int idx = col;
                float x = startX + col * (slotSizePx + paddingPx);
                float y = startY;

                // Highlight
                if (idx == selectedSlotIdx) batch.setColor(0.6f, 0.6f, 0.6f, 1f);
                batch.draw(slotBackground, x, y, slotSizePx, slotSizePx);
                batch.setColor(1f, 1f, 1f, 1f);

                ItemStack stack = inventory.getSlot(idx);
                if (stack != null) {
                    drawStack(batch, stack, x, y, slotSizePx);
                }
            }

        } else {
            // Inventar-Overlay mit Hintergrund

            // 1) Skalierung: 1px im PNG entspricht scale UI‑Units
            //    Wir nehmen hier die Breite eines Slot‑Cells im PNG, d.h. slotBackground.getWidth()
            float cellPx = slotBackground.getWidth();
            float scale  = slotSizePx / cellPx * uiSize;

            // 2) Hintergrundgröße in UI‑Units
            float bgWidth  = inventoryBackground.getWidth()  * scale;
            float bgHeight = inventoryBackground.getHeight() * scale;

            // 3) Zentriert
            float bgX = (vw - bgWidth)  / 2f;
            float bgY = (vh - bgHeight) / 2f;

            // 4) Background zeichnen
            batch.draw(inventoryBackground, bgX, bgY, bgWidth, bgHeight);

            // 5) Innen-Abstand (Ränder im PNG, hier 5px) umrechnen
            float sidePadding = 5f * scale;

            // 6) Zwischenraum zwischen Slots entspricht ebenfalls slotBackground.getWidth()*scale
            float cellUi = cellPx * scale;

            // 7) Grid-Startkoordinate (oberste, linkeste Slot-Position)
            int cols = inventory.getColumns();
            int rows = inventory.getRows();
            float gridStartX = bgX + sidePadding;
            float gridStartY = bgY + bgHeight - sidePadding * 2 - cellUi * 4;

            // 8) Hotbar rendern
            int row = 0;
            for (int col = 0; col < cols; col++) {
                int idx = row * cols + col;
                float x = gridStartX + col * cellUi;
                float y = gridStartY - cellUi * 4 - sidePadding;

                // Draw item stack
                ItemStack stack = inventory.getSlot(idx);
                if (stack != null) {
                    drawStack(batch,
                        stack,
                        x,
                        y,
                        cellUi);
                }
            }

            // 9) Alle restlichen rendern
            for (row = 1; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int idx = row * cols + col;
                    float x = gridStartX + col * cellUi;
                    float y = gridStartY - row * cellUi;

                    // Draw item stack
                    ItemStack stack = inventory.getSlot(idx);
                    if (stack != null) {
                        drawStack(batch,
                            stack,
                            x,
                            y,
                            cellUi);
                    }
                }
            }
        }
    }

    /** Hilfsmethode zum Zeichnen eines ItemStacks im Slot */
    private void drawStack(SpriteBatch batch, ItemStack stack, float x, float y, float size) {
        float iconSize = size * 0.7f;
        float offset   = size * 0.1f;
        batch.draw(stack.getItem().getIcon(), x + offset, y + offset, iconSize, iconSize);
        font.setUseIntegerPositions(false);
        font.getData().setScale(size * 0.025f);
        font.draw(batch, String.valueOf(stack.getAmount()), x + size * 0.5f, y + size * 0.25f);
    }
}
