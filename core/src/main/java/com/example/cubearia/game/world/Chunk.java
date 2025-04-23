package com.example.cubearia.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.example.cubearia.game.Settings;

public class Chunk {
    public static final int WIDTH  = 16;
    public static final int HEIGHT = 128;

    public final BlockType[][] blocks = new BlockType[WIDTH][HEIGHT];
    private final World world;
    private final int chunkX;

    public Chunk(int chunkX, BiomeType prevBiome, BiomeType biome, BiomeType nextBiome, World world) {
        this.chunkX = chunkX;
        this.world  = world;
        generateTerrain(prevBiome, biome, nextBiome);
    }

    private void generateTerrain(BiomeType prev, BiomeType biome, BiomeType next) {
        int[] raw = Settings.FLATWORLD
            ? getFlatHeights()
            : getRawHeights(chunkX, biome);

        if (!Settings.FLATWORLD) {
            int[] heights = raw.clone();
            smoothEdges(raw, heights, prev, biome, next);
            raw = heights;
        }

        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            boolean surfaceDone = false;

            for (int y = 0; y < HEIGHT; y++) {
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    blocks[x][y] = biome.getBlockAtDepth(depth);

                    if (!surfaceDone && y == terrainHeight - 1) {
                        int globalX = chunkX * WIDTH + x;
                        if (Settings.STRUCTURES) {
                            generateStructures(globalX, y, biome);
                        }
                        surfaceDone = true;
                    }
                } else if (y < Settings.SEA_LEVEL) {
                    blocks[x][y] = BlockType.WATER;
                } else {
                    blocks[x][y] = BlockType.AIR;
                }
            }
            // Bedrock:
            blocks[x][0] = BlockType.BEDROCK;
        }
    }

    private int[] getFlatHeights() {
        int[] h = new int[WIDTH];
        int level = Settings.SEA_LEVEL + 2;
        for (int i = 0; i < WIDTH; i++) h[i] = level;
        return h;
    }

    private int[] getRawHeights(int chunkX, BiomeType biome) {
        int[] raw = new int[WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            int globalX = chunkX * WIDTH + x;
            int base  = (int)Noise.getHeight(globalX, biome.getFluctuation());
            int water = biome.getWaterDepth();
            raw[x] = water > 0 ? base - water : base;
        }
        return raw;
    }

    private void smoothEdges(int[] raw, int[] heights, BiomeType prev, BiomeType b, BiomeType next) {
        if (b.name().contains("OZEAN")) return;
        int blend = 8;

        if (prev != null) {
            int[] leftRaw  = getRawHeights(chunkX - 1, prev);
            float ih = (leftRaw[WIDTH - 1] + raw[0]) / 2f;
            for (int i = 0; i < blend; i++) {
                float t = 1 - (i+1)/(float)(blend+1);
                heights[i] = (int)(raw[i]*(1-t) + ih*t);
            }
        }
        if (next != null) {
            int[] rightRaw = getRawHeights(chunkX + 1, next);
            float ih = (rightRaw[0] + raw[WIDTH-1]) / 2f;
            for (int i = 0; i < blend; i++) {
                float t = 1 - (i+1)/(float)(blend+1);
                heights[WIDTH-1-i] = (int)(raw[WIDTH-1-i]*(1-t) + ih*t);
            }
        }
    }

    public void generateStructures(int globalX, int y, BiomeType biome) {
        // TREES
        if (biome == BiomeType.FOREST) {
            world.queueStructure("Tree.tmx", globalX - 2, y + 1, 6);
        } else if (biome == BiomeType.PLAINS) {
            world.queueStructure("Tree.tmx", globalX - 2, y + 1, 10);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chunk[x=").append(chunkX).append("]\n");
        for (int y = HEIGHT-1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(blocks[x][y].name().charAt(0));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
