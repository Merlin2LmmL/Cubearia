package com.example.cubearia.game.world;

public enum BiomeType {
    // Ocean variants
    COLD_OCEAN(
        0.05f, 0.0f, 3, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.ICE_BLOCK, 1),
            new Layer(BlockType.WATER, 4),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.FLOATING_ICEBERG, 1, 0.1f, 2, 2)
        }
    ),
    OCEAN(
        0.05f, 0.5f, 3, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 5),
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.SUNKEN_SHIP, 2, 0.05f, 2, 2)
        }
    ),
    WARM_OCEAN(
        0.05f, 1.0f, 3, BiomeGroup.OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 5),
            new Layer(BlockType.SAND, 5),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.CORAL, 15, 0.3f, 2, 2),
            new Structure(StructureType.CLAM_COLONY, 3, 0.2f, 1, 1)
        }
    ),

    // Shallow-Ocean variants
    COLD_SHALLOW_OCEAN(
        0.175f, 0.0f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.ICE_BLOCK, 1),
            new Layer(BlockType.WATER, 2),
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.ICE_FISHING_HOLE, 1, 0.1f, 1, 1)
        }
    ),
    SHALLOW_OCEAN(
        0.175f, 0.5f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 2),
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.SEAGRASS, 25, 0.4f, 1, 1)
        }
    ),
    WARM_SHALLOW_OCEAN(
        0.175f, 1.0f, 3, BiomeGroup.SHALLOW_OCEAN,
        new Layer[]{
            new Layer(BlockType.WATER, 2),
            new Layer(BlockType.SAND, 4),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{}
    ),

    // Beach variants
    COLD_BEACH(
        0.2f, 0.0f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.STONE, 2),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{}
    ),
    BEACH(
        0.2f, 0.5f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SAND, 5),
            new Layer(BlockType.DIRT, 2),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{}
    ),
    WARM_BEACH(
        0.2f, 1.0f, 2, BiomeGroup.BEACH,
        new Layer[]{
            new Layer(BlockType.SAND, 6),
            new Layer(BlockType.DIRT, 2),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.SEA_SHELL, 2, 0.15f, 1, 1)
        }
    ),
    // Standard biomes
    ICE_SPIKES(
        0.48f, 0.10f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.ICE_BLOCK, 2),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.ICE_SPIKE, 1, 0.05f, 2, 3)
        }
    ),
    SNOWY_TAIGA(
        0.48f, 0.20f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.IGLOO, 1, 0.02f, 3, 2),
        }
    ),
    SNOWY_PLAINS(
        0.48f, 0.30f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.SNOWMAN, 1, 0.05f, 0, 0)
        }
    ),
    SWAMP(
        0.48f, 0.40f, 4, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1),
            new Layer(BlockType.DIRT, 2),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.SWAMP_HUT, 1, 0.02f, 3, 2),
            new Structure(StructureType.MUD_POOL, 5, 0.25f, 1, 1)
        }
    ),
    PLAINS(
        0.48f, 0.50f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.TREE, 10, 0.1f, 2, 1),
            new Structure(StructureType.HAY_BALE, 5, 0.3f, 0, 0)
        }
    ),
    FOREST(
        0.48f, 0.60f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.TREE, 6, 0.3f, 2, 1),
            new Structure(StructureType.FALLEN_TREE, 3, 0.2f, 1, 1)
        }
    ),
    FLOWER_FOREST(
        0.48f, 0.70f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.BEEHIVE_TREE, 2, 0.2f, 1, 1),
            new Structure(StructureType.FLOWER, 2, 0.2f, 1, 1)
        }
    ),
    SAVANNA(
        0.48f, 0.80f, 16, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.GRASSBLOCK, 1),
            new Layer(BlockType.DIRT, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.GRASS, 0, 0.7f, 0, 1),
            new Structure(StructureType.SAVANNA_TREE, 6, 0.3f, 1, 1)
        }
    ),
    JUNGLE(
        0.48f, 1.0f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.JUNGLE_TREE, 1, 0.01f, 3, 3),
            new Structure(StructureType.CACAO_TREE, 1, 0.05f, 1, 1)
        }
    ),
    DESERT(
        0.48f, 1.0f, 8, BiomeGroup.STANDARD,
        new Layer[]{
            new Layer(BlockType.SAND, 3),
            new Layer(BlockType.SANDSTONE, 8),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.CACTUS, 2, 0.2f, 0, 1),
            new Structure(StructureType.DESERT_WELL, 1, 0.01f, 3, 3),
            new Structure(StructureType.FOSSIL, 1, 0.05f, 1, 1)
        }
    ),

    // Hills
    COLD_HILL(
        0.73f, 0.0f, 32, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{}
    ),
    HILL(
        0.73f, 0.5f, 32, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.DIRT, 4),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.HILL_CAVE, 2, 0.1f, 2, 2)
        }
    ),
    BADLANDS(
        0.73f, 1.0f, 24, BiomeGroup.HILL,
        new Layer[]{
            new Layer(BlockType.RED_SAND, 1),
            new Layer(BlockType.TERRACOTTA, 4),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.MINE_SHAFT, 1, 0.05f, 2, 2)
        }
    ),

    // Mountains
    SNOWY_MOUNTAIN(
        0.90f, 0.0f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.SNOW_BLOCK, 1),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{}
    ),
    MOUNTAIN(
        0.90f, 0.5f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.MINE_SHAFT, 1, 0.02f, 3, 2)
        }
    ),
    VOLCANIC_MOUNTAIN(
        0.90f, 1.0f, 64, BiomeGroup.MOUNTAIN,
        new Layer[]{
            new Layer(BlockType.VOLCANIC_ROCK, 3),
            new Layer(BlockType.STONE, Integer.MAX_VALUE)
        },
        new Structure[]{
            new Structure(StructureType.LAVA_SEA, 0, 0.1f, 0, 0),
            new Structure(StructureType.SCORCHED_TREE, 1, 0.05f, 1, 1),
            new Structure(StructureType.VOLCANIC_VENT, 2, 0.1f, 1, 1)
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
    public Layer[] getLayers() { return layers; }
    public Structure[] getStructures() { return structures; }

    public BlockType getBlockAtDepth(int depth) {
        int current = 0;
        for (Layer layer : layers) {
            if (depth < current + layer.height) {
                return layer.block;
            }
            current += layer.height;
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

    public static class Layer {
        public final BlockType block;
        public final int height;
        public Layer(BlockType block, int height) {
            this.block = block;
            this.height = height;
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
