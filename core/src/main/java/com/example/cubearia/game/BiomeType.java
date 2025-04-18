package com.example.cubearia.game;

public enum BiomeType {
    // Normale Biome
    DESERT          (8, Category.NORMAL,
        new Layer(BlockType.SAND, 5),
        new Layer(BlockType.SAND, 3),                   // SANDSTONE
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SAVANNA         (16, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    BADLANDS        (24, Category.NORMAL,
        new Layer(BlockType.SAND, 2),                   // REDSAND
        new Layer(BlockType.STONE, 4),                  // TERRACOTTA
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    JUNGLE          (8, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    PLAINS          (8, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    FORREST         (8, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SWAMP           (4, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    FLOWER_FORREST  (8, Category.NORMAL,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SNOWY_TAIGA     (8, Category.NORMAL,
        new Layer(BlockType.IRONORE, 1),                // SNOWBLOCK
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SNOWY_PLAINS    (8, Category.NORMAL,
        new Layer(BlockType.IRONORE, 1),                // SNOWBLOCK
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    ICE_SPICES      (8, Category.NORMAL,
        new Layer(BlockType.DIAMONDORE, 2),             // PACKED ICE
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Ozean-Biome
    OCEAN           (0, Category.OCEAN,
        new Layer(BlockType.WATER_SURFACE, 1),
        new Layer(BlockType.WATER_FULL, 10),
        new Layer(BlockType.SAND, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Gebirgs-Biome
    MOUNTAIN        (64, Category.MOUNTAIN,
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SNOWY_MOUNTAIN  (64, Category.MOUNTAIN,
        new Layer(BlockType.IRONORE, 1),                // SNOW
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    VOLCANIC_MOUNTAIN(64, Category.MOUNTAIN,
        new Layer(BlockType.STONE, Integer.MAX_VALUE)); // warmes Gebirge-Biom

    public enum Category {
        OCEAN,
        NORMAL,
        MOUNTAIN
    }

    private final int fluctuation;
    private final Category category;
    private final Layer[] layers;

    BiomeType(int fluctuation, Category category, Layer... layers) {
        this.fluctuation = fluctuation;
        this.category = category;
        this.layers = layers;
    }

    public BlockType getBlockAtDepth(int depth) {
        int currentDepth = 0;
        for (Layer layer : layers) {
            if (depth < currentDepth + layer.height) {
                return layer.block;
            }
            currentDepth += layer.height;
        }
        return layers[layers.length - 1].block;
    }

    public int getFluctuation() {
        return fluctuation;
    }

    public Category getCategory() {
        return category;
    }
}
