package com.example.cubearia.game.world;

/**
 * Block-Typen inklusive dynamischer Wasser-Textur: Surface vs. Full
 */
public enum BlockType {
    AIR             (0, 0, false,  false,    0),
    GRASSBLOCK      (1, 0, true,   true,     4),
    DIRT            (2, 0, true,   true,     3),
    STONE           (3, 0, true,   true,     12),
    COAL            (4, 0, true,   true,     15),
    GOLDORE         (5, 0, true,   true,     18),
    IRONORE         (6, 0, true,   true,     20),
    DIAMONDORE      (7, 0, true,   true,     30),
    COBBLESTONE     (0, 1, true,   true,     13),
    BEDROCK         (1, 1, false,  true,     Integer.MAX_VALUE),
    LOG             (2, 1, true,   true,     6),
    PLANK           (3, 1, true,   true,     5),
    TERRACOTTA      (4, 1, true,   true,     12),
    RED_SAND        (5, 1, true,   true,     6),
    SANDSTONE       (6, 1, true,   true,     11),
    SAND            (7, 1, true,   true,     6),
    CACTUS          (0, 2, true,   false,    2),
    LEAF            (1, 1, true,   false,    1),
    GRASS           (2, 2, true,   false,    0),
    BLUEBERRIES     (3, 2, true,   false,    0),
    TULIP           (4, 2, true,   false,    0),
    FIRE_FLOWER     (5, 2, true,   false,    0),
    ROSE            (6, 2, true,   false,    0),
    HYACINTH        (7, 2, true,   false,    0),
    SNOW_BLOCK      (0, 3, true,   true ,    3),
    ICE_BLOCK       (1, 3, true,   true ,    2),
    WATER           (2, 3, false,  false,    Integer.MAX_VALUE),
    LAVA            (3, 3, false,  false,    Integer.MAX_VALUE),
    VOLCANIC_ROCK   (4, 3, true,   true,     15),
    OBSIDIAN        (5, 3, true,   true,     50),
    CRAFTING_TABLE  (6, 3, true,   true,     7),
    FURNACE         (7, 3, true,   true,     11)
        {
    };

    // Basis-Felder für alle Block-Typen
    protected final int tileX;
    protected final int tileY;
    private final boolean breakable;
    private final boolean solid;
    public final double hardness;

    BlockType(int tileX, int tileY, boolean breakable, boolean solid, double hardness) {
        this.tileX     = tileX;
        this.tileY     = tileY;
        this.breakable = breakable;
        this.solid     = solid;
        this.hardness  = hardness;
    }

    /**
     * Standard-Getter für X-Koordinate im Tileset.
     * Bei WATER wird ggf. die Oberflächen-Variante gewählt.
     */
    public int getTileX(World world, int x, int y) {
        return tileX;
    }

    /**
     * Standard-Getter für Y-Koordinate im Tileset.
     */
    public int getTileY(World world, int x, int y) {
        return tileY;
    }

    // Alte Getter bleiben erhalten, liefern Basis-Koordinaten
    public int getTileX() {
        return tileX;
    }
    public int getTileY() {
        return tileY;
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

    public int getID() {
        return tileX * (tileY + 1);
    }

    public static BlockType getBlockByID(int id) {
        BlockType[] vals = values();
        if (id >= 0 && id < vals.length) {
            return vals[id];
        } else {
            return AIR;
        }
    }
}
