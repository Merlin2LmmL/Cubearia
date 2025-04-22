package com.example.cubearia.game.world;

public enum BiomeType {
    // Ocean variants
    COLD_OCEAN   (0.05f, 0.0f, 3,
        new Layer(BlockType.ICE_BLOCK, 1),
        new Layer(BlockType.WATER, 9),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    OCEAN        (0.05f, 0.5f, 3,
        new Layer(BlockType.WATER, 10),
        new Layer(BlockType.SAND, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    WARM_OCEAN   (0.05f, 1.0f, 3,
        new Layer(BlockType.WATER, 10),
        new Layer(BlockType.SAND, 5),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Shallow-Ocean variants
    COLD_SHALLOW_OCEAN(0.15f, 0.0f, 3,
        new Layer(BlockType.ICE_BLOCK, 1),
        new Layer(BlockType.WATER, 4),
        new Layer(BlockType.SAND, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SHALLOW_OCEAN     (0.15f, 0.5f, 3,
        new Layer(BlockType.WATER, 5),
        new Layer(BlockType.SAND, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    WARM_SHALLOW_OCEAN(0.15f, 1.0f, 3,
        new Layer(BlockType.WATER, 5),
        new Layer(BlockType.SAND, 4),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Beach variants
    COLD_BEACH   (0.25f, 0.0f, 1,
        new Layer(BlockType.SAND, 3),
        new Layer(BlockType.STONE, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    BEACH        (0.25f, 0.5f, 1,
        new Layer(BlockType.SAND, 5),
        new Layer(BlockType.DIRT, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    WARM_BEACH   (0.25f, 1.0f, 1,
        new Layer(BlockType.SAND, 6),
        new Layer(BlockType.DIRT, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Plains / Standard biomes
    ICE_SPIKES   (0.4f, 0.0f, 8,
        new Layer(BlockType.ICE_BLOCK, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SNOWY_PLAINS (0.42f, 0.05f, 8,
        new Layer(BlockType.SNOW_BLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SNOWY_TAIGA  (0.44f, 0.10f, 8,
        new Layer(BlockType.SNOW_BLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SWAMP        (0.46f, 0.20f, 4,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 2),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    FLOWER_FOREST(0.48f, 0.30f, 8,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    PLAINS       (0.5f, 0.40f, 8,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    FOREST       (0.52f, 0.60f, 8,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    SAVANNA      (0.54f, 0.70f, 16,
        new Layer(BlockType.GRASSBLOCK, 1),
        new Layer(BlockType.DIRT, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    DESERT       (0.58f, 1.0f, 8,
        new Layer(BlockType.SAND, 5),
        new Layer(BlockType.SANDSTONE, 8),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Hills
    COLD_HILL    (0.75f, 0.0f, 32,
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    HILL         (0.78f, 0.5f, 32,
        new Layer(BlockType.DIRT, 4),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    BADLANDS     (0.56f, 0.80f, 24,
        new Layer(BlockType.RED_SAND, 1),
        new Layer(BlockType.TERRACOTTA, 4),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),

    // Mountain variants
    SNOWY_MOUNTAIN   (0.92f, 0.0f, 64,
        new Layer(BlockType.SNOW_BLOCK, 1),
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    MOUNTAIN         (0.95f, 0.5f, 64,
        new Layer(BlockType.STONE, Integer.MAX_VALUE)),
    VOLCANIC_MOUNTAIN(0.98f, 1.0f, 64,
        new Layer(BlockType.VOLCANIC_ROCK, 3),
        new Layer(BlockType.STONE, Integer.MAX_VALUE));

    private final float categoryValue;
    private final float temperatureValue;
    private final int fluctuation;
    private final Layer[] layers;

    BiomeType(float categoryValue, float temperatureValue, int fluctuation, Layer... layers) {
        this.categoryValue = categoryValue;
        this.temperatureValue = temperatureValue;
        this.fluctuation = fluctuation;
        this.layers = layers;
    }

    public float getCategoryValue() {
        return categoryValue;
    }

    public float getTemperatureValue() {
        return temperatureValue;
    }

    public int getFluctuation() {
        return fluctuation;
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

    public int getWaterDepth() {
        int depth = 0;
        for (Layer layer : layers) {
            if (layer.block == BlockType.WATER) {
                depth += layer.height;
            }
        }
        return depth;
    }
}
