package com.example.cubearia.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.example.cubearia.game.world.BlockType;

public class Assets {
    public static TilesetLoader  tileset;
    public static Texture        blockTexture;
    public static Texture        playerTexture;
    public static Texture        slotBackground;
    public static Texture        inventoryBackground;
    public static Texture        blackPixel;

    public static TextureRegion[] breakingFrames;
    public static Animation<TextureRegion> breakingAnimation;

    // Maps
    public static TiledMap cactiMap;
    public static TiledMap iceSpikeMap;
    public static TiledMap lavaSeaMap;
    public static TiledMap treeMap;
    public static OrthogonalTiledMapRenderer cactiMapRenderer;
    public static OrthogonalTiledMapRenderer iceSpikeMapRenderer;
    public static OrthogonalTiledMapRenderer lavaSeaMapRenderer;
    public static OrthogonalTiledMapRenderer treeMapRenderer;

    public static void load() {
        // 1) Lade das Tileset
        tileset = new TilesetLoader("Blocks.tsx");
        for (BlockType block : BlockType.values()) {
            block.resolve(tileset);
        }

        // 2) Generiere den Black Pixel
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.BLACK);
        pix.fill();
        blackPixel = new Texture(pix);
        pix.dispose();

        // 3) Lade andere Assets
        playerTexture = new Texture(Gdx.files.internal("Player.png"));
        slotBackground = new Texture(Gdx.files.internal("ui/SlotBackground.png"));
        inventoryBackground = new Texture(Gdx.files.internal("ui/InventoryBackground.png"));

        // 4) Lade die Animation für den Breaking-Effekt
        Texture breakingSheet = new Texture(Gdx.files.internal("BreakingAnimation.png"));
        breakingFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            breakingFrames[i] = new TextureRegion(breakingSheet, i * 16, 0, 16, 16);
        }
        breakingAnimation = new Animation<>(0.1f, breakingFrames);

        // 5) Lade TMX-Maps
        loadMaps();
    }

    private static void loadMaps() {
        // Lade die Maps mit TmxMapLoader
        cactiMap = new TmxMapLoader().load("Cacti.tmx");
        iceSpikeMap = new TmxMapLoader().load("Ice Spike.tmx");
        lavaSeaMap = new TmxMapLoader().load("Lava Sea.tmx");
        treeMap = new TmxMapLoader().load("Tree.tmx");

        // Erstelle Renderer für jede Map
        cactiMapRenderer = new OrthogonalTiledMapRenderer(cactiMap);
        iceSpikeMapRenderer = new OrthogonalTiledMapRenderer(iceSpikeMap);
        lavaSeaMapRenderer = new OrthogonalTiledMapRenderer(lavaSeaMap);
        treeMapRenderer = new OrthogonalTiledMapRenderer(treeMap);
    }

    public static void dispose() {
        // Entsorge alle Assets
        blockTexture.dispose();
        playerTexture.dispose();
        slotBackground.dispose();
        inventoryBackground.dispose();

        // Entsorge TMX-Maps und Renderer
        cactiMap.dispose();
        iceSpikeMap.dispose();
        lavaSeaMap.dispose();
        treeMap.dispose();

        cactiMapRenderer.dispose();
        iceSpikeMapRenderer.dispose();
        lavaSeaMapRenderer.dispose();
        treeMapRenderer.dispose();

        // Entsorge das Breaking-Animation-Sheet
        breakingAnimation.getKeyFrames()[0].getTexture().dispose();  // Entsorge die Textur der Animation
    }
}
