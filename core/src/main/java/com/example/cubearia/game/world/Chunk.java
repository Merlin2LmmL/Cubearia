package com.example.cubearia.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.example.cubearia.game.Settings;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 128;
    private World world;

    private final int chunkX;
    public final BlockType[][] blocks = new BlockType[WIDTH][HEIGHT];

    public Chunk(int chunkX, BiomeType prevBiome, BiomeType biome, BiomeType nextBiome, World world) {
        this.world = world;
        this.chunkX = chunkX;
        generateTerrain(prevBiome, biome, nextBiome);
    }

    private void generateTerrain(BiomeType prev, BiomeType biome, BiomeType next) {
        int[] raw = getRawHeights(chunkX, biome);

        if (!Settings.FLATWORLD) {
            int[] heights = raw.clone();
            smoothEdges(raw, heights, prev, biome, next);
            raw = heights;
        }

        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            boolean surfaceGenerated = false;  // Um sicherzustellen, dass nur der oberste Block die Struktur erzeugt

            for (int y = 0; y < HEIGHT; y++) {
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    blocks[x][y] = biome.getBlockAtDepth(depth);

                    // Wenn der Block an der Oberfläche ist (der erste Block in dieser Spalte)
                    if (!surfaceGenerated && y == terrainHeight - 1) {
                        generateStructures(x, y);  // Aufruf der Struktur-Generierung

                        surfaceGenerated = true;  // Markiere, dass die Struktur generiert wurde
                    }
                } else if (y < Settings.SEA_LEVEL) {
                    blocks[x][y] = BlockType.WATER;
                } else {
                    blocks[x][y] = BlockType.AIR;
                }
            }
            blocks[x][0] = BlockType.BEDROCK;
        }
    }

    private int[] getRawHeights(int chunkX, BiomeType biome) {
        int[] raw = new int[WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            int globalX = chunkX * WIDTH + x;
            // Terrain-Höhe inklusive Fluktuation
            int baseHeight = (int) Noise.getHeight(globalX, biome.getFluctuation());
            int waterDepth = biome.getWaterDepth();
            if (waterDepth > 0) {
                // Meeresboden = Terrain-Höhe minus Wasser-Tiefe
                raw[x] = baseHeight - waterDepth;
            } else {
                raw[x] = baseHeight;
            }
        }
        return raw;
    }
    private void smoothEdges(int[] raw, int[] heights, BiomeType prev, BiomeType biome, BiomeType next) {
        if (biome.name().contains("OZEAN")) return;
        final int blendWidth = 8;

        if (prev != null) {
            int[] leftRaw = getRawHeights(chunkX - 1, prev);
            int prevBorder = leftRaw[WIDTH - 1];
            int thisBorder = raw[0];
            float interfaceHeight = (prevBorder + thisBorder) / 2.0f;

            for (int i = 0; i < blendWidth; i++) {
                float t = 1.0f - (i + 1) / (float)(blendWidth + 1);
                heights[i] = (int)(raw[i] * (1 - t) + interfaceHeight * t);
            }
        }

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

    public void generateStructures(int x, int y) {
        if (MathUtils.random(1, 1000) == 1) {
            world.loadStructure("Tree.tmx", x * chunkX, y + 4);
        }
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
