package com.example.cubearia.game.world;

public enum BiomeType {
    // Ocean variants
    COLD_OCEAN(
        0.05f, 0.0f, 2, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 8, 1),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.FLOATING_ICEBERG, 6, 0.05f, 2, 3)
        }
    ),
    OCEAN(
        0.05f, 0.5f, 3, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 8, 1),
            new Layer(BlockType.SAND, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SEAGRASS, 1, 0.3f, 0, -7),
            new Structure(StructureType.SUNKEN_SHIP, 10, 0.02f, 2, -8)
        }
    ),
    WARM_OCEAN(
        0.05f, 1.0f, 3, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 8, 1),
            new Layer(BlockType.SAND, 6, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.CORAL, 4, 0.1f, 2, -4),
            new Structure(StructureType.SEAGRASS, 1, 0.3f, 0, -7)
        }
    ),

    // Shallow-Ocean variants
    COLD_SHALLOW_OCEAN(
        0.175f, 0.0f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 5, 1),
            new Layer(BlockType.SAND, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.ICE_FISHING_HOLE, 1, 0.01f, 0, 3)
        }
    ),
    SHALLOW_OCEAN(
        0.175f, 0.5f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 5, 1),
            new Layer(BlockType.SAND, 5, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SEAGRASS, 1, 0.3f, 0, -4)
        }
    ),
    WARM_SHALLOW_OCEAN(
        0.175f, 1.0f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 5, 1),
            new Layer(BlockType.SAND, 4, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.CORAL, 4, 0.1f, 2, -4),
            new Structure(StructureType.SEAGRASS, 1, 0.3f, 0, -4)
        }
    ),

    // Beach variants
    COLD_BEACH(
        0.2f, 0.0f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.SAND, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SUNKEN_SHIP, 80, 0.01f, 2, -1)
        }
    ),
    BEACH(
        0.2f, 0.5f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SAND, 1, 0),
            new Layer(BlockType.DIRT, 2, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SUNKEN_SHIP, 80, 0.005f, 2, -1),
            new Structure(StructureType.SEA_SHELL, 2, 0.005f, 1, 1)
        }
    ),
    WARM_BEACH(
        0.2f, 1.0f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SAND, 1, 0),
            new Layer(BlockType.DIRT, 2, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SEA_SHELL, 2, 0.01f, 1, 1)
        }
    ),

    // Standard biomes
    ICE_SPIKES(
        0.48f, 0.10f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.ICE_BLOCK, 2, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.ICE_SPIKE, 20, 0.05f, 2, 0)
        }
    ),
    SNOWY_TAIGA(
        0.48f, 0.20f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.IGLOO, 10, 0.01f, 2, 1)
        }
    ),
    SNOWY_PLAINS(
        0.48f, 0.30f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SNOWMAN, 10, 0.01f, 0, 1),
            new Structure(StructureType.TREE, 10, 0.015f, 2, 1)
        }
    ),
    SWAMP(
        0.48f, 0.40f, 4, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 2, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 8, 0.1f, 0, 1),
            new Structure(StructureType.SWAMP_HUT, 10, 0.002f, 2, -5),
            new Structure(StructureType.MUD_POOL, 10, 0.02f, 5, -3)
        }
    ),
    PLAINS(
        0.48f, 0.50f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.2f, 0, 1),
            new Structure(StructureType.FLOWER, 2, 0.1f, 0, 1),
            new Structure(StructureType.TREE, 5, 0.3f, 2, 1)
        }
    ),
    FOREST(
        0.48f, 0.60f, 6, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.01f, 0, 1),
            new Structure(StructureType.FLOWER, 1, 0.01f, 0, 1),
            new Structure(StructureType.TREE, 5, 0.8f, 2, 1)
        }
    ),
    FLOWER_FOREST(
        0.48f, 0.70f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.2f, 0, 1),
            new Structure(StructureType.FLOWER, 1, 0.66f, 0, 1)
        }
    ),
    SAVANNA(
        0.48f, 0.80f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.2f, 0, 1),
            new Structure(StructureType.SAVANNA_TREE, 6, 0.2f, 3, 1)
        }
    ),
    JUNGLE(
        0.48f, 1.0f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 2, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.2f, 0, 1),
            new Structure(StructureType.JUNGLE_TREE, 5, 0.2f, 2, 1),
            new Structure(StructureType.HUGE_JUNGLE_TREE, 7, 0.2f, 4, 1)
        }
    ),
    DESERT(
        0.48f, 1.0f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SAND, 1, 0),
            new Layer(BlockType.SAND, 3, 0),
            new Layer(BlockType.SANDSTONE, 8, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.CACTUS, 2, 0.2f, 0, 1),
            new Structure(StructureType.DESERT_WELL, 5, 0.02f, 2, -3),
            new Structure(StructureType.FOSSIL, 15, 0.03f, 3, 1)
        }
    ),

    // Hills
    COLD_HILL(
        0.73f, 0.0f, 24, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.SNOWMAN, 10, 0.005f, 0, 1),
            new Structure(StructureType.TREE, 10, 0.0725f, 2, 1)
        }
    ),
    HILL(
        0.73f, 0.5f, 24, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1, 0),
            new Layer(BlockType.DIRT, 4, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 1, 0.1f, 0, 1),
            new Structure(StructureType.FLOWER, 2, 0.05f, 0, 1),
            new Structure(StructureType.TREE, 5, 0.15f, 2, 1)
        }
    ),
    BADLANDS(
        0.73f, 1.0f, 24, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.RED_SAND, 1, 0),
            new Layer(BlockType.TERRACOTTA, 4, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.MINE_SHAFT, 50, 0.02f, 20, -15)
        }
    ),

    // Mountains
    SNOWY_MOUNTAIN(
        0.90f, 0.0f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.MINE_SHAFT, 50, 0.002f, 20, -20)
        }
    ),
    MOUNTAIN(
        0.90f, 0.5f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.STONE, 1, -1),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.MINE_SHAFT, 50, 0.002f, 20, -20)
        }
    ),
    VOLCANIC_MOUNTAIN(
        0.90f, 1.0f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.VOLCANIC_ROCK, 1, 0),
            new Layer(BlockType.VOLCANIC_ROCK, 3, 0),
            new Layer(BlockType.STONE, Integer.MAX_VALUE, -1)
        },
        new Structure[]{
            new Structure(StructureType.LAVA_SEA, 0, 0.1f, 0, 0),
            new Structure(StructureType.SCORCHED_TREE, 4, 0.02f, 3, 1),
            new Structure(StructureType.VOLCANIC_VENT, 20, 0.01f, -4, -19)
        }
    );

    private final float categoryValue;
    private final float temperatureValue;
    private final int fluctuation;
    private final BiomeGroup group;
    private final Layer[] layers;
    private final Structure[] structures;

    BiomeType(float categoryValue, float temperatureValue, int fluctuation, BiomeGroup group, Layer[] layers, Structure[] structures) {
        this.categoryValue = categoryValue;
        this.temperatureValue = temperatureValue;
        this.fluctuation = fluctuation;
        this.group = group;
        this.layers = layers;
        this.structures = structures;
    }

    public float getCategoryValue() { return categoryValue; }
    public float getTemperatureValue() { return temperatureValue; }
    public int getFluctuation() { return fluctuation; }
    public BiomeGroup getGroup() { return group; }
    public Structure[] getStructures() { return structures; }

    public Layer getLayerAtDepth(int depth) {
        int current = 0;
        for (Layer layer : layers) {
            if (depth < current + layer.height) {
                return layer;
            }
            current += layer.height;
        }
        return layers[layers.length - 1];
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

    public static class Layer {
        public final BlockType block;
        public final int height;
        public final int targetLayer; // 0 = vorderer Layer, 1 = hinterer Layer, -1 = beide Layer

        public Layer(BlockType block, int height) {
            this(block, height, 1); // Standard: hinterer Layer für Terrain
        }

        public Layer(BlockType block, int height, int targetLayer) {
            this.block = block;
            this.height = height;
            this.targetLayer = targetLayer;
        }
    }

    public enum BiomeGroup {
        OCEAN,
        SHALLOW_OCEAN,
        BEACH,
        STANDARD,
        HILL,
        MOUNTAIN
    }
}
