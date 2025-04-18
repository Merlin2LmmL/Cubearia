package com.example.cubearia.game;

public enum BlockType {
    AIR             (0, 0, false,   false,0),
    GRASSBLOCK      (1, 0, true,    true, 5),
    DIRT            (2, 0, true,    true, 5),
    STONE           (3, 0, true,    true, 15),
    BEDROCK         (0, 1, false,   true, 2147483647),
    LOG             (1, 1, true,    true, 8),
    LEAF            (2, 1, true,    false,2),
    PLANK           (3, 1, true,    true, 7),
    GRASS           (0, 2, true,    false,1),
    GOLDORE         (1, 2, true,    true, 18),
    IRONORE         (2, 2, true,    true, 20),
    DIAMONDORE         (3, 2, true,    true, 30),
    CRAFTING_TABLE  (0, 3, true,    true, 7),
    SAND            (1, 3, true,    true, 6),
    WATER_FULL      (2, 3, false,   false, 2147483647),
    WATER_SURFACE   (3, 3, false,   false, 2147483647);

    private final int tileX;
    private final int tileY;
    private final boolean breakable;
    private final boolean solid;
    public final double hardness;

    BlockType(int tileX, int tileY, boolean breakable, boolean solid, double hardness) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.breakable = breakable;
        this.solid = solid;
        this.hardness = hardness;
    }

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

    public int getID() {
        return tileX*(tileY + 1);
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
