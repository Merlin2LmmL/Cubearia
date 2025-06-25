package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.world.World;

/**
 * Einfache DebugRenderer-Klasse, die Spieler-Position und FPS
 * anzeigt, solange sie aktiviert ist.
 */
public class DebugRenderer {
    private final BitmapFont font;
    private boolean enabled;
    private final Vector2 uiPosition;

    public DebugRenderer() {
        font = new BitmapFont();
        font.getData().setScale(1f);
        uiPosition = new Vector2(10f, Gdx.graphics.getHeight() - 10f);
        enabled = false;
    }

    /** Debug-Anzeige umschalten */
    public void toggle() {
        enabled = !enabled;
    }

    /**
     * Zeichnet Player-Position und FPS in UI-Koordinaten.
     * Muss im UI-Pass gerendert werden (uiCamera.combined).
     */
    public void render(SpriteBatch batch, Player player, BlockType blockAtCursor, World world) {
        if (!enabled) return;

        // Skaliere die Schriftgröße basierend auf der Bildschirmgröße
        float vw = Gdx.graphics.getWidth();
        float vh = Gdx.graphics.getHeight();
        float scale = Math.min(vw / 1920f, vh / 1080f) * 0.7f; // Basis-Skalierung
        font.getData().setScale(scale);

        // Position ganz oben links
        float x = 10f * (vw / 1920f); // Skaliere Position
        float y = vh - 10f * (vh / 1080f); // Ganz oben
        float lineHeight = font.getLineHeight();
        
        // Player-Position
        font.draw(batch,
            String.format("Player:  (X=%.2f, Y=%.2f, velX=%.2f, velY=%.2f", player.x, player.y, player.getVelocityX(), player.getVelocityY()),
            x, y
        );

        // Block-Information
        font.draw(batch,
            String.format("Blocktype: %s", blockAtCursor.name()),
            x, (y - lineHeight)
        );

        // FPS unterhalb der ersten Zeile
        font.draw(batch,
            String.format("FPS: %d", Gdx.graphics.getFramesPerSecond()),
            x, (y - lineHeight * 2)
        );

        // BIOME
        font.draw(batch,
            String.format("Biome: %s", world.getBiomeTypeAt((int) player.x)),
            x, (y - lineHeight * 3)
        );

        // Layer-Information
        font.draw(batch,
            String.format("Layer: %d (TAB to switch)", player.getSelectedLayer() + 1),
            x, (y - lineHeight * 4)
        );
        
        // Schriftgröße zurücksetzen
        font.getData().setScale(1f);
    }

    /** Ressourcen freigeben */
    public void dispose() {
        font.dispose();
    }
}
