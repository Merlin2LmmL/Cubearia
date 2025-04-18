package com.example.cubearia.game;

import java.util.Arrays;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 128;

    // Glättungseinstellungen
    private static final int SMOOTH_PASSES = 2;
    // Meeresspiegel für Seen/Lakes
    private static final int SEA_LEVEL = 64;
    // Anzahl Sand-Schichten am Ufer
    private static final int BEACH_DEPTH = 2;

    private final int chunkX;
    public final BlockType[][] blocks = new BlockType[WIDTH][HEIGHT];

    public Chunk(int chunkX, BiomeType biome) {
        this.chunkX = chunkX;
        generateTerrain(biome);
    }

    private void generateTerrain(BiomeType biome) {
        int[] terrainHeights = getSmoothedHeights(chunkX, biome);

        for (int localX = 0; localX < WIDTH; localX++) {
            int terrainHeight = terrainHeights[localX];

            for (int y = 0; y < HEIGHT; y++) {
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    blocks[localX][y] = biome.getBlockAtDepth(depth);
                } else {
                    blocks[localX][y] = BlockType.AIR;
                }
            }
        }
    }


    private int[] getSmoothedHeights(int chunkX, BiomeType biome) {
        int[] rawHeights = new int[Chunk.WIDTH];

        // 1. Roh-Höhen mit Noise
        for (int x = 0; x < Chunk.WIDTH; x++) {
            int globalX = chunkX * Chunk.WIDTH + x;
            rawHeights[x] = (int) Noise.getHeight(globalX, biome.getFluctuation());
        }

        // 2. Glättung (Moving Average, radius=1)
        int[] smoothed = new int[Chunk.WIDTH];
        for (int x = 0; x < Chunk.WIDTH; x++) {
            int sum = 0;
            int count = 0;
            for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx;
                if (nx >= 0 && nx < Chunk.WIDTH) {
                    sum += rawHeights[nx];
                    count++;
                }
            }
            smoothed[x] = sum / count;
        }

        return smoothed;
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
