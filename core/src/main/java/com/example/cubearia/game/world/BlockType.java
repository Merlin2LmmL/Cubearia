package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.TilesetLoader;

public enum BlockType {
    AIR             (0, false, false, 0,        false,  true),
    GRASSBLOCK      (1, true,  true,  4,        false,  false),
    DIRT            (2, true,  true,  3,        false,  false),
    STONE           (3, true,  true,  12,       false,  false),
    COAL            (4, true,  true,  15,       false,  false),
    GOLDORE         (5, true,  true,  18,       false,  false),
    IRONORE         (6, true,  true,  20,       false,  false),
    DIAMONDORE      (7, true,  true,  30,       false,  false),
    COBBLESTONE     (8, true,  true,  13,       false,  false),
    BEDROCK         (9, false, true, Integer.MAX_VALUE,  false,  false),
    OAK_LOG         (10,true,  true,  6,        false,  false),
    OAK_PLANK       (11,true,  true,  5,        false,  false),
    TERRACOTTA      (12,true,  true,  12,       false,  false),
    RED_SAND        (13,true,  true,  6,        false,  false),
    SANDSTONE       (14,true,  true,  11,       false,  false),
    SAND            (15,true,  true,  6,        false,  false),
    CACTUS          (16,true,  false, 2,        false,  true),
    LEAF            (17,true,  false, 1,        false,  true),
    GRASS           (18,true,  false, 0,        false,  true),
    BLUEBERRIES     (19,true,  false, 0,        false,  true),
    TULIP           (20,true,  false, 0,        false,  true),
    FIRE_FLOWER     (21,true,  false, 0,        false,  true),
    ROSE            (22,true,  false, 0,        false,  true),
    HYACINTH        (23,true,  false, 0,        false,  true),
    SNOW_BLOCK      (24,true,  true , 3,        false,  true),
    ICE_BLOCK       (25,true,  true , 2,        false,  true),
    WATER           (26,false, false, Integer.MAX_VALUE, true,   true),
    LAVA            (27,false, false, Integer.MAX_VALUE, true,   true),
    VOLCANIC_ROCK   (28,true,  true,  15,       false,  false),
    OBSIDIAN        (29,true,  true,  50,       false,  false),
    CRAFTING_TABLE  (30,true,  true,  7,        false,  false),
    FURNACE         (31,true,  true,  11,       false,  false),
    BLUE_CORAL      (32,true,  true,  3,        false,  false),
    PINK_CORAL      (33,true,  true,  3,        false,  false),
    RED_CORAL       (34,true,  true,  3,        false,  false),
    YELLOW_CORAL    (35,true,  true,  3,        false,  false),
    SEA_GRASS       (36,true,  false, 0,        false,  true),
    NAUTILUS_SHELL  (37,true,  false, 1,        false,  true),
    GREEN_OYSTER    (38,true,  false, 1,        false,  true),
    PUMPKIN         (39,true,  true,  4,        false,  false),
    MUD             (40,false, false, Integer.MAX_VALUE, true,   true),
    HAY_BALE        (41,true,  true,  2,        false,  false),
    ACACIA_LOG      (42,true,  true,  6,        false,  false),
    JUNGLE_LOG      (43,true,  true,  6,        false,  false),
    CACAO_PLANT     (44,true,  false, 1,        false,  true),
    BONE_BLOCK      (45,true,  true,  7,        false,  true),
    FENCE           (46,true,  false, 8,        false,  true);

    private final int tilesetId;
    private final boolean breakable;
    private final boolean solid;
    private final double hardness;
    private final boolean fluid;
    private final boolean transparent;
    private TextureRegion region;

    BlockType(int tilesetId, boolean breakable, boolean solid, double hardness, boolean fluid, boolean transparent) {
        this.tilesetId = tilesetId;
        this.breakable = breakable;
        this.solid     = solid;
        this.hardness  = hardness;
        this.fluid     = fluid;
        this.transparent = transparent;
    }

    public void resolve(TilesetLoader loader) {
        this.region = loader.getRegionById(tilesetId);
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

    public boolean isTransparent() {
        return transparent;
    }

    public static BlockType getById(int id) {
        for (BlockType b : values()) {
            if (b.tilesetId == id) return b;
        }
        return AIR;
    }
}
