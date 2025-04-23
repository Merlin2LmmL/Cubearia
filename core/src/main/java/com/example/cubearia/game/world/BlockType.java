package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.TilesetLoader;

public enum BlockType {
    AIR             (  0, false, false,  0, false),
    GRASSBLOCK      (  1, true,  true,   4, false),
    DIRT            (  2, true,  true,   3, false),
    STONE           (  3, true,  true,  12, false),
    COAL            (  4, true,  true,  15, false),
    GOLDORE         (  5, true,  true,  18, false),
    IRONORE         (  6, true,  true,  20, false),
    DIAMONDORE      (  7, true,  true,  30, false),
    COBBLESTONE     (  8, true,  true,  13, false),
    BEDROCK         (  9, false, true, Integer.MAX_VALUE, false),
    LOG             ( 10, true,  true,   6, false),
    PLANK           ( 11, true,  true,   5, false),
    TERRACOTTA      ( 12, true,  true,  12, false),
    RED_SAND        ( 13, true,  true,   6, false),
    SANDSTONE       ( 14, true,  true,  11, false),
    SAND            ( 15, true,  true,   6, false),
    CACTUS          ( 16, true,  false,  2, false),
    LEAF            ( 17, true,  false,  1, false),
    GRASS           ( 18, true,  false,  0, false),
    BLUEBERRIES     ( 19, true,  false,  0, false),
    TULIP           ( 20, true,  false,  0, false),
    FIRE_FLOWER     ( 21, true,  false,  0, false),
    ROSE            ( 22, true,  false,  0, false),
    HYACINTH        ( 23, true,  false,  0, false),
    SNOW_BLOCK      ( 24, true,  true ,  3, false),
    ICE_BLOCK       ( 25, true,  true ,  2, false),
    WATER           ( 26, false, false, Integer.MAX_VALUE, true),
    LAVA            ( 27, false, false, Integer.MAX_VALUE, true),
    VOLCANIC_ROCK   ( 28, true,  true,  15, false),
    OBSIDIAN        ( 29, true,  true,  50, false),
    CRAFTING_TABLE  ( 30, true,  true,   7, false),
    FURNACE         ( 31, true,  true,  11, false);

    private final int tilesetId;
    private final boolean breakable;
    private final boolean solid;
    private final double hardness;
    private final boolean fluid;
    private TextureRegion region;

    BlockType(int tilesetId, boolean breakable, boolean solid, double hardness, boolean fluid) {
        this.tilesetId = tilesetId;
        this.breakable = breakable;
        this.solid     = solid;
        this.hardness  = hardness;
        this.fluid     = fluid;
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

    public boolean isFluid() {
        return fluid;
    }

    public static BlockType getById(int id) {
        for (BlockType b : values()) {
            if (b.tilesetId == id) return b;
        }
        return AIR;
    }
}
