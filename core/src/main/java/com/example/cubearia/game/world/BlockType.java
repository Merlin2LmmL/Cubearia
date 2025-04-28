package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.TilesetLoader;

public enum BlockType {
    AIR             (0,  false, false,  0,                 false,  true,   "AIR",          -1, 0),
    GRASSBLOCK      (1,  true,  true,   4,                 false,  false,  "DIRT",          5, 0),
    DIRT            (2,  true,  true,   3,                 false,  false,  "DIRT",          5, 0),
    STONE           (3,  true,  true,   12,                false,  false,  "COBBLESTONE",   3, 1),
    COAL            (4,  true,  true,   15,                false,  false,  "COAL",          3, 1),
    GOLDORE         (5,  true,  true,   18,                false,  false,  "GOLDORE",       3, 3),
    IRONORE         (6,  true,  true,   20,                false,  false,  "IRONORE",       3, 2),
    DIAMONDORE      (7,  true,  true,   30,                false,  false,  "DIAMOND",       3, 3),
    COBBLESTONE     (8,  true,  true,   13,                false,  false,  "COBBLESTONE",   3, 1),
    BEDROCK         (9,  false, true,   Integer.MAX_VALUE,          false,  false,  "BEDROCK",      -1, 0),
    OAK_LOG         (10, true,  true,   6,                 false,  false,  "OAK_LOG",       4, 0),
    OAK_PLANK       (11, true,  true,   5,                 false,  false,  "OAK_PLANK",     4, 0),
    TERRACOTTA      (12, true,  true,   12,                false,  false,  "TERRACOTTA",    3, 1),
    RED_SAND        (13, true,  true,   6,                 false,  false,  "RED_SAND",      5, 0),
    SANDSTONE       (14, true,  true,   11,                false,  false,  "SANDSTONE",     3, 1),
    SAND            (15, true,  true,   6,                 false,  false,  "SAND",          5, 0),
    CACTUS          (16, true,  false,  2,                 false,  true,   "CACTUS",        6, 0),
    LEAF            (17, true,  false,  1,                 false,  true,   "LEAF",            7, 1),
    GRASS           (18, true,  false,  0,                 false,  true,   "GRASS",            7, 1),
    BLUEBERRIES     (19, true,  false,  0,                 false,  true,   "BLUEBERRIES",   6, 0),
    TULIP           (20, true,  false,  0,                 false,  true,   "TULIP",         6, 0),
    FIRE_FLOWER     (21, true,  false,  0,                 false,  true,   "FIRE_FLOWER",   6, 0),
    ROSE            (22, true,  false,  0,                 false,  true,   "ROSE",          6, 0),
    HYACINTH        (23, true,  false,  0,                 false,  true,   "HYACINTH",      6, 0),
    SNOW_BLOCK      (24, true,  true,   3,                 false,  true,   "SNOW_BLOCK",            5, 1),
    ICE_BLOCK       (25, true,  true,   2,                 false,  true,   "ICE_BLOCK",            3, 1),
    WATER           (26, false, false, Integer.MAX_VALUE,           true,   true,   "WATER",        -1, 0),
    LAVA            (27, false, false, Integer.MAX_VALUE,           true,   true,   "LAVA",         -1, 0),
    VOLCANIC_ROCK   (28, true,  true,   15,                false,  false,  "VOLCANIC_ROCK", 3, 1),
    OBSIDIAN        (29, true,  true,   50,                false,  false,  "OBSIDIAN",      3, 4),
    CRAFTING_TABLE  (30, true,  true,   7,                 false,  false,  "CRAFTING_TABLE",4, 0),
    FURNACE         (31, true,  true,   11,                false,  false,  "FURNACE",       3, 1),
    BLUE_CORAL      (32, true,  true,   3,                 false,  false,  "BLUE_CORAL",    3, 1),
    PINK_CORAL      (33, true,  true,   3,                 false,  false,  "PINK_CORAL",    3, 1),
    RED_CORAL       (34, true,  true,   3,                 false,  false,  "RED_CORAL",     3, 1),
    YELLOW_CORAL    (35, true,  true,   3,                 false,  false,  "YELLOW_CORAL",  3, 1),
    SEA_GRASS       (36, true,  false,  0,                 false,  true,   "SEA_GRASS",     7, 1),
    NAUTILUS_SHELL  (37, true,  false,  1,                 false,  true,   "NAUTILUS_SHELL",6, 0),
    GREEN_OYSTER    (38, true,  false,  1,                 false,  true,   "GREEN_OYSTER",  6, 0),
    PUMPKIN         (39, true,  true,   4,                 false,  false,  "PUMPKIN",       4, 0),
    MUD             (40, false, false, Integer.MAX_VALUE,           true,   true,   "MUD",          -1, 0),
    HAY_BALE        (41, true,  true,   2,                 false,  false,  "HAY_BALE",      6, 0),
    ACACIA_LOG      (42, true,  true,   6,                 false,  false,  "ACACIA_LOG",    4, 0),
    JUNGLE_LOG      (43, true,  true,   6,                 false,  false,  "JUNGLE_LOG",    4, 0),
    CACAO_PLANT     (44, true,  false,  1,                 false,  true,   "CACAO_PLANT",   6, 0),
    BONE_BLOCK      (45, true,  true,   7,                 false,  true,   "BONE_BLOCK",    3, 1),
    FENCE           (46, true,  false,  10,                false,  true,   "FENCE",         4, 0);

    private final int     tilesetId;
    private final boolean breakable;
    private final boolean solid;
    private final float   hardness;
    private final boolean fluid;
    private final boolean transparent;
    private final String  droppingItem;
    private TextureRegion region;
    private int           preferredTool;
    private int           minLevel;

    /**
     * Konstruktor mit droppingItemName als String (Name einer Konstanten).
     */
    BlockType(int tilesetId,
              boolean breakable,
              boolean solid,
              float hardness,
              boolean fluid,
              boolean transparent,
              String droppingItem,
              int preferredTool,    // 2: Sword, 3: Pickaxe, 4: Axe, 5: Shovel, 6: Hoe, 7: Sheers
              int minLevel) {
        this.tilesetId      = tilesetId;
        this.breakable      = breakable;
        this.solid          = solid;
        this.hardness       = hardness;
        this.fluid          = fluid;
        this.transparent    = transparent;
        this.droppingItem   = droppingItem;
        this.preferredTool  = preferredTool;
        this.minLevel       = minLevel;
    }

    /**
     * Liefert den BlockType, der beim Abbau gedroppt wird.
     * Wenn droppingItemName ungültig ist, fällt auf sich selbst zurück.
     */
    public String getDroppingItem() {
        return droppingItem;
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

    public float getHardness() {
        return hardness;
    }

    public boolean isFluid() {
        return fluid;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public int getPreferredTool() {
        return preferredTool;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public static BlockType getById(int id) {
        for (BlockType b : values()) {
            if (b.tilesetId == id) return b;
        }
        return AIR;
    }
    private BlockType getSelf() {return this;}
}
