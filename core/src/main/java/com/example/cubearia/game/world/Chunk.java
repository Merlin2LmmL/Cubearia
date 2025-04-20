package com.example.cubearia.game.world;

import com.example.cubearia.game.Settings;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 128;

    private static final int SEA_LEVEL   = 64;
    private static final int OCEAN_DEPTH = 24;

    private final int chunkX;
    public final BlockType[][] blocks = new BlockType[WIDTH][HEIGHT];

    public Chunk(int chunkX, BiomeType prevBiome, BiomeType biome, BiomeType nextBiome) {
        this.chunkX = chunkX;
        generateTerrain(prevBiome, biome, nextBiome);
    }

    private void generateTerrain(BiomeType prev, BiomeType biome, BiomeType next) {
        int[] raw;

        if (Settings.FLATWORLD) {
            raw = new int[WIDTH];
            for (int i = 0; i < raw.length; i++) {
                raw[i] = 64;
            }
        } else {
            raw = getRawHeights(chunkX, biome);
        }

        if (!Settings.FLATWORLD) {
            int[] heights = raw.clone();
            smoothEdges(raw, heights, prev, biome, next);
            raw = heights;
        }

        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            for (int y = 0; y < HEIGHT; y++) {
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    if (Settings.FLATWORLD) {
                        if (y == 63) {
                            blocks[x][y] = BlockType.GRASSBLOCK;
                        } else if (y > 58) {
                            blocks[x][y] = BlockType.DIRT;
                        } else {
                            blocks[x][y] = BlockType.STONE;
                        }
                    } else {
                        blocks[x][y] = biome.getBlockAtDepth(depth);
                    }
                } else if (y < SEA_LEVEL) {
                    blocks[x][y] = BlockType.WATER;
                } else {
                    blocks[x][y] = BlockType.AIR;
                }
                blocks[x][0] = BlockType.BEDROCK;
            }
        }
    }

    /**
     * Glättet die ersten und letzten blendWidth Spalten so, dass
     * raw[0] auf den Nachbar-Wert prevBorder trifft und raw[WIDTH-1]
     * auf nextBorder, mit einer sanften linearen Interpolation.
     */
    private void smoothEdges(int[] raw, int[] heights, BiomeType prev, BiomeType biome, BiomeType next) {
        if (biome == BiomeType.OCEAN) return;

        final int blendWidth = 4;

        // —— Linke Kante ——
        if (prev != null) {
            int[] leftRaw = getRawHeights(chunkX - 1, prev);
            int prevBorder = leftRaw[WIDTH - 1];
            int thisBorder = raw[0];
            float interfaceHeight = (prevBorder + thisBorder) / 2.0f;

            for (int i = 0; i < blendWidth; i++) {
                // t: Gewichtung für den Schnittpunkt (je näher an der Grenze, desto größer)
                float t = 1.0f - (i + 1) / (float)(blendWidth + 1);
                heights[i] = (int)(raw[i] * (1 - t) + interfaceHeight * t);
            }
        }

        // —— Rechte Kante ——
        if (next != null) {
            int[] rightRaw = getRawHeights(chunkX + 1, next);
            int nextBorder = rightRaw[0];
            int thisBorder = raw[WIDTH - 1];
            float interfaceHeight = (nextBorder + thisBorder) / 2.0f;

            for (int i = 0; i < blendWidth; i++) {
                float t = 1.0f - (i + 1) / (float)(blendWidth + 1);
                heights[WIDTH - 1 - i] = (int)(raw[WIDTH - 1 - i] * (1 - t) + interfaceHeight * t);
            }
        }
    }

    private int[] getRawHeights(int chunkX, BiomeType biome) {
        int[] raw = new int[WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            int globalX = chunkX * WIDTH + x;
            int h = (int) Noise.getHeight(globalX, biome.getFluctuation());
            if (biome == BiomeType.OCEAN) {
                int max = SEA_LEVEL - 1;
                int min = SEA_LEVEL - OCEAN_DEPTH;
                h = Math.max(min, Math.min(max, h));
            }
            raw[x] = h;
        }
        return raw;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chunk[x=").append(chunkX).append("]\n");
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(blocks[x][y].name().charAt(0));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
