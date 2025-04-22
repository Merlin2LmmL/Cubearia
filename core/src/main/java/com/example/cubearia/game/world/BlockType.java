package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.TilesetLoader;

public enum BlockType {
    AIR             (  0, false, false,  0),
    GRASSBLOCK      (  1, true,  true,   4),
    DIRT            (  2, true,  true,   3),
    STONE           (  3, true,  true,  12),
    COAL            (  4, true,  true,  15),
    GOLDORE         (  5, true,  true,  18),
    IRONORE         (  6, true,  true,  20),
    DIAMONDORE      (  7, true,  true,  30),
    COBBLESTONE     (  8, true,  true,  13),
    BEDROCK         (  9, false, true, Integer.MAX_VALUE),
    LOG             ( 10, true,  true,   6),
    PLANK           ( 11, true,  true,   5),
    TERRACOTTA      ( 12, true,  true,  12),
    RED_SAND        ( 13, true,  true,   6),
    SANDSTONE       ( 14, true,  true,  11),
    SAND            ( 15, true,  true,   6),
    CACTUS          ( 16, true,  false,  2),
    LEAF            ( 17, true,  false,  1),
    GRASS           ( 18, true,  false,  0),
    BLUEBERRIES     ( 19, true,  false,  0),
    TULIP           ( 20, true,  false,  0),
    FIRE_FLOWER     ( 21, true,  false,  0),
    ROSE            ( 22, true,  false,  0),
    HYACINTH        ( 23, true,  false,  0),
    SNOW_BLOCK      ( 24, true,  true ,  3),
    ICE_BLOCK       ( 25, true,  true ,  2),
    WATER           ( 26, false, false, Integer.MAX_VALUE),
    LAVA            ( 27, false, false, Integer.MAX_VALUE),
    VOLCANIC_ROCK   ( 28, true,  true,  15),
    OBSIDIAN        ( 29, true,  true,  50),
    CRAFTING_TABLE  ( 30, true,  true,   7),
    FURNACE         ( 31, true,  true,  11);

    private final int tilesetId;
    private final boolean breakable;
    private final boolean solid;
    private final double hardness;
    private TextureRegion region;

    BlockType(int tilesetId, boolean breakable, boolean solid, double hardness) {
        this.tilesetId = tilesetId;
        this.breakable = breakable;
        this.solid     = solid;
        this.hardness  = hardness;
    }

    public void resolve(TilesetLoader loader) {
        this.region = loader.getRegionById(tilesetId);
        if (region == null) {
            throw new RuntimeException("BlockType [" + name() + "] konnte nicht auf ID " + tilesetId + " gemappt werden.");
        }
    }

    public TextureRegion getRegion() {
        return region;
    }

    public int getId() {
        return tilesetId;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isSolid() {
        return solid;
    }

    public double getHardness() {
        return hardness;
    }

    public static BlockType getById(int id) {
        for (BlockType b : values()) {
            if (b.tilesetId == id) return b;
        }
        return AIR;
    }
}
