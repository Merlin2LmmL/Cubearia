package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

import static com.example.cubearia.game.Settings.UISCALE;
import static com.example.cubearia.game.entities.Player.*;

public class HotbarUI {
    private final Inventory inventory;
    private final Texture slotBackground;
    private final BitmapFont font;

    /**
     * @param inventory      Das Inventory, aus dem die ItemStacks gezogen werden.
     */
    public HotbarUI(Inventory inventory) {
        this.inventory      = inventory;
        this.slotBackground = Assets.slotBackground;
        this.font = new BitmapFont();
        this.font.getData().setScale(0.8f);
    }

    /**
     * Zeichnet die Hotbar: zuerst die Slot‑Hintergründe, dann die jeweiligen Items.
     */
    public void render(SpriteBatch batch, OrthographicCamera uiCam, int selectedSlot) {
        float vw    = uiCam.viewportWidth;
        float vh    = uiCam.viewportHeight;
        float scale = Settings.UISCALE * 0.75f;   // 0…1, 1 = maximaler Platzverbrauch
        float selectedSlotScale = 1.1f;

        // Jeder Slot soll 'scale' Anteil der Bildschirmbreite insgesamt nutzen:
        //   totalWidth = vw * scale
        // und davon 9 gleich große Slots:
        float totalWidth = vw * scale;
        float slotSize   = totalWidth / 9f;

        // Horizontal zentrieren:
        float startX = (vw - totalWidth) / 2f;
        // 5% Abstand vom unteren Rand:
        float startY = vh * 0.05f;

        for (int i = 0; i < 9; i++) {
            float x = startX + i * slotSize;
            // Slot-Hintergrund zeichnen
            if (i != selectedSlot) {
                batch.setColor(0.8f, 0.8f, 0.8f, 0.8f);
                batch.draw(slotBackground, x, startY, slotSize, slotSize);
                batch.setColor(1f, 1f, 1f, 1f);
            }

            // Berechne die neue Breite und Höhe des Slots nach der Skalierung
            float scaledWidth = slotSize * selectedSlotScale;
            float scaledHeight = slotSize * selectedSlotScale;

            // Berechne die Anpassungen, um den Slot zu zentrieren
            float adjustedX = startX + selectedSlot * slotSize - (scaledWidth - slotSize) / 2;
            float adjustedY = startY - (scaledHeight - slotSize) / 2;

            // Zeichne den Slot mit der skalieren Größe und angepassten Position
            batch.draw(slotBackground, adjustedX, adjustedY, scaledWidth, scaledHeight);
        }
        for (int i = 0; i < 9; i++) {
            float x = startX + i * slotSize;
            // gegebenenfalls Item im Slot zeichnen
            ItemStack stack = inventory.getSlot(i);
            if (stack != null) {
                stack.drawStack(batch, x, startY, slotSize);
            }
        }
    }

    /**
     * Zeichnet die Hotbar mit Layer-Information.
     */
    public void render(SpriteBatch batch, OrthographicCamera uiCam, Player player) {
        render(batch, uiCam, player.getSelectedSlot());
        
        // Layer-Information anzeigen
        float vw = uiCam.viewportWidth;
        float vh = uiCam.viewportHeight;
        
        String layerText = "Layer: " + (player.getSelectedLayer() + 1);
        
        // Skaliere die Schriftgröße basierend auf der Bildschirmgröße - viel größer
        float scale = Math.min(vw / 1920f, vh / 1080f) * 2.0f; // Größere Basis-Skalierung
        font.getData().setScale(scale);
        
        float textX = vw * 0.02f; // 2% vom linken Rand
        float textY = vh * 0.15f; // 15% vom unteren Rand
        
        // Berechne die tatsächliche Textgröße
        float textWidth = font.draw(batch, layerText, 0, 0, 0, 0, false).width;
        float textHeight = font.getLineHeight();
        
        // Hintergrund für bessere Lesbarkeit - größer und prominenter
        batch.setColor(0f, 0f, 0f, 0.7f); // Dunklerer Hintergrund
        float padding = 10f * scale; // Mehr Padding
        batch.draw(Assets.slotBackground, textX - padding, textY - textHeight - padding, 
                  textWidth + 2 * padding, textHeight + 2 * padding);
        batch.setColor(1f, 1f, 1f, 1f);
        
        // Layer-Text zeichnen - größer und farbiger
        font.setColor(player.getSelectedLayer() == 0 ? Color.CYAN : Color.ORANGE);
        font.draw(batch, layerText, textX, textY);
        font.setColor(1f, 1f, 1f, 1f);
        
        // Schriftgröße zurücksetzen
        font.getData().setScale(0.8f);
    }
}
