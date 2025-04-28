package com.example.cubearia.game.inventory;

public enum ItemID {
    BOW             (2, "bow",              1,  2, 3),
    STICK           (3, "stick",            64, 1, 0),
    WOODEN_SWORD    (4, "wooden_sword",     1,  2, 1),
    WOODEN_PICKAXE  (5, "wooden_pickaxe",   1,  3, 1),
    WOODEN_AXE      (6, "wooden_axe",       1,  4, 1),
    WOODEN_SHOVEL   (7, "wooden_shovel",    1,  5, 1),
    WOODEN_HOE      (8, "wooden_hoe",       1,  6, 1),
    ARROW           (10, "arrow",           64, 1, 0),
    COAL            (11, "coal",            64, 1, 0),
    STONE_SWORD     (12, "stone_sword",     1,  2, 2),
    STONE_PICKAXE   (13, "stone_pickaxe",   1,  3, 2),
    STONE_AXE       (14, "stone_axe",       1,  4, 2),
    STONE_SHOVEL    (15, "stone_shovel",    1,  5, 2),
    STONE_HOE       (16, "stone_hoe",       1,  6, 2),
    IRON_INGOT      (19, "iron_ingot",      64, 1, 0),
    IRON_SWORD      (20, "iron_sword",      1,  2, 3),
    IRON_PICKAXE    (21, "iron_pickaxe",    1,  3, 3),
    IRON_AXE        (22, "iron_axe",        1,  4, 3),
    IRON_SHOVEL     (23, "iron_shovel",     1,  5, 3),
    IRON_HOE        (24, "iron_hoe",        1,  6, 3),
    GOLD_INGOT      (27, "gold_ingot",      64, 1, 0),
    GOLD_SWORD      (28, "gold_sword",      1,  2, 4),
    GOLD_PICKAXE    (29, "gold_pickaxe",    1,  3, 4),
    GOLD_AXE        (30, "gold_axe",        1,  4, 4),
    GOLD_SHOVEL     (31, "gold_shovel",     1,  5, 4),
    GOLD_HOE        (32, "gold_hoe",        1,  6, 4),
    DIAMOND         (35, "diamond",         64, 1, 0),
    DIAMOND_SWORD   (36, "diamond_sword",   1,  2, 5),
    DIAMOND_PICKAXE (37, "diamond_pickaxe", 1,  3, 5),
    DIAMOND_AXE     (38, "diamond_axe",     1,  4, 5),
    DIAMOND_SHOVEL  (39, "diamond_shovel",  1,  5, 5),
    DIAMOND_HOE     (40, "diamond_hoe",     1,  6, 5);

    private final int id;
    private final String name;
    private final int maxStackSize;
    private final int type;
    private final int toolLevel;

    /*
    TYPES:
        1: CRAFTING
        2: COMBAT
        3: PICKAXE
        4: AXE
        5: SHOVEL
        6: HOE
        7: SHEERS
        8: ARMOR
     */

    ItemID(int id, String name, int maxStackSize, int type, int toolLevel) {
        this.id = id;
        this.name = name;
        this.maxStackSize = maxStackSize;
        this.type = type;
        this.toolLevel = toolLevel;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getType() {
        return type;
    }

    public int getToolLevel() {
        return toolLevel;
    }

    public static ItemID fromId(int id) {
        for (ItemID item : values()) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }
}
