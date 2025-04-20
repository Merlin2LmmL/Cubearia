package com.example.cubearia.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;

public class Assets {
    public static Texture        blockTexture;
    public static TextureAtlas   blockAtlas;        // ← new
    public static Texture        playerTexture;
    public static Texture        slotBackground;
    public static Texture        inventoryBackground;
    public static Texture        blackPixel;

    public static TextureRegion[] breakingFrames;
    public static Animation<TextureRegion> breakingAnimation;

    public static void load() {
        // 1) Load your raw block PNG
        blockTexture    = new Texture(Gdx.files.internal("Blocks.png"));

        // Black‑Pixel generieren
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.BLACK);
        pix.fill();
        blackPixel = new Texture(pix);
        pix.dispose();

        // 2) Wrap the *entire* sheet as one atlas‐region named "tiles"
        blockAtlas      = new TextureAtlas();
        TextureRegion tilesRegion = new TextureRegion(
            blockTexture,
            0, 0,
            blockTexture.getWidth(),
            blockTexture.getHeight()
        );
        blockAtlas.addRegion("tiles", tilesRegion);

        // 3) Everything else as before
        playerTexture   = new Texture(Gdx.files.internal("Player.png"));
        slotBackground  = new Texture(Gdx.files.internal("ui/SlotBackground.png"));
        inventoryBackground  = new Texture(Gdx.files.internal("ui/InventoryBackground.png"));

        Texture breakingSheet = new Texture(Gdx.files.internal("BreakingAnimation.png"));
        breakingFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            breakingFrames[i] = new TextureRegion(breakingSheet, i * 16, 0, 16, 16);
        }
        breakingAnimation = new Animation<>(0.1f, breakingFrames);
    }

    public static void dispose() {
        blockTexture.dispose();
        blockAtlas.dispose();         // ← don’t forget to clean up your atlas
        playerTexture.dispose();
        slotBackground.dispose();
        // (also dispose breakingSheet if you’ve stored it as a field)
    }
}
