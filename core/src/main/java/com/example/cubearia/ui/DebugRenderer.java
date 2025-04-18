package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.example.cubearia.game.BlockType;
import com.example.cubearia.game.Player;
import com.example.cubearia.game.Chunk;
import com.example.cubearia.game.World;

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

        // Berechnung der UI-Position relativ zur UI-Kamera (wir nutzen die Skalierung hier)
        float x = uiPosition.x;
        float y = uiPosition.y;

        // Sicherstellen, dass die UI-Position auch bei Resize immer korrekt bleibt:
        // Die Berechnungen berücksichtigen jetzt die Skalierung der UI-Kamera

        // Player-Position
        font.draw(batch,
            String.format("Player:  (X=%.2f, Y=%.2f, velX=%.2f, velY=%.2f", player.x, player.y, player.velocityX, player.velocityY),
            x, y
        );

        // Block-Information
        font.draw(batch,
            String.format("Blocktype: %s", blockAtCursor.name()),
            x, (y - font.getLineHeight())
        );

        // FPS unterhalb der ersten Zeile
        font.draw(batch,
            String.format("FPS: %d", Gdx.graphics.getFramesPerSecond()),
            x, (y - font.getLineHeight() * 2)
        );

        // BIOME
        font.draw(batch,
            String.format("FPS: %s", world.getBiomeTypeAt((int) player.x)),
            x, (y - font.getLineHeight() * 3)
        );
    }

    /** Ressourcen freigeben */
    public void dispose() {
        font.dispose();
    }
}
