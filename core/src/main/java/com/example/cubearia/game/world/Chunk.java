package com.example.cubearia.game.world;

import com.example.cubearia.game.Settings;
import java.util.Random;

/**
 * Ein Chunk repräsentiert einen Abschnitt der Welt mit Terrain, Höhlen und optionalen Strukturen.
 */
public class Chunk {
    public static final int WIDTH  = 32;
    public static final int HEIGHT = 128;

    public final BlockType[][] blocks = new BlockType[WIDTH][HEIGHT];
    private final World world;
    private final int chunkX;

    public Chunk(int chunkX, BiomeType prevBiome, BiomeType biome, BiomeType nextBiome, World world) {
        this.chunkX = chunkX;
        this.world  = world;
        generateTerrain(prevBiome, biome, nextBiome);
    }

    /**
     * Erzeugt das Terrain, generiert Höhlen und queued Strukturen an der Oberfläche.
     */
    private void generateTerrain(BiomeType prev, BiomeType biome, BiomeType next) {
        int[] raw = Settings.FLATWORLD
            ? getFlatHeights()
            : getRawHeights(chunkX, biome);

        if (!Settings.FLATWORLD) {
            int[] heights = raw.clone();
            smoothEdges(raw, heights, prev, biome, next);
            raw = heights;
        }

        // Terrain-Füllung
        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            boolean surfaceDone = false;

            for (int y = 0; y < HEIGHT; y++) {
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    blocks[x][y] = biome.getBlockAtDepth(depth);

                    // Oberfläche: Strukturen queue’n
                    if (!surfaceDone && y == terrainHeight - 1 && Settings.STRUCTURES) {
                        int globalX = chunkX * WIDTH + x;
                        generateStructures(globalX, y, biome);
                        surfaceDone = true;
                    }

                } else if (y == Settings.SEA_LEVEL) {
                    blocks[x][y] = (biome == BiomeType.COLD_OCEAN ? BlockType.ICE_BLOCK : BlockType.WATER);
                } else if (y < Settings.SEA_LEVEL) {
                    blocks[x][y] = BlockType.WATER;
                } else {
                    blocks[x][y] = BlockType.AIR;
                }
            }

            // Bedrock immer am Boden
            blocks[x][0] = BlockType.BEDROCK;
        }

        // Höhlen generieren
        generateCaves();
    }

    private int[] getFlatHeights() {
        int[] h = new int[WIDTH];
        int level = Settings.SEA_LEVEL + 2;
        for (int i = 0; i < WIDTH; i++) {
            h[i] = level;
        }
        return h;
    }

    private int[] getRawHeights(int chunkX, BiomeType biome) {
        int[] raw = new int[WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            int globalX = chunkX * WIDTH + x;
            int base  = (int) Noise.getHeight(globalX, biome.getFluctuation());
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
                float t = 1 - (i + 1) / (float) (blend + 1);
                heights[i] = (int) (raw[i] * (1 - t) + ih * t);
            }
        }
        if (next != null) {
            int[] rightRaw = getRawHeights(chunkX + 1, next);
            float ih = (rightRaw[0] + raw[WIDTH - 1]) / 2f;
            for (int i = 0; i < blend; i++) {
                float t = 1 - (i + 1) / (float) (blend + 1);
                heights[WIDTH - 1 - i] = (int) (raw[WIDTH - 1 - i] * (1 - t) + ih * t);
            }
        }
    }

    /**
     * Queued eine Struktur, Abstand zum nächsten prüfend.
     */
    public void generateStructures(int globalX, int y, BiomeType biome) {
        for (Structure s : biome.getStructures()) {
            long structureSeed = computeStructureSeed(globalX, y, s);
            Random rand = new Random(structureSeed);
            if (rand.nextFloat() < s.getSpawnProbability()) {
                int spawnX = globalX - s.getTrunkXOffset();
                world.queueStructure(
                    s.getFilePath(),
                    spawnX,
                    y + s.getTrunkYOffset(),
                    s.getMinDistance()
                );
            }
        }
    }

    private long computeStructureSeed(int x, int y, Structure s) {
        long seed = Settings.SEED;
        seed = seed * 31L + x;
        seed = seed * 31L + y;
        seed = seed * 31L + s.getFilePath().hashCode();
        return seed;
    }

    // --- NEU: Höhlen-Generierung ---

    private void generateCaves() {
        // Pro Chunk 1–3 Tunnel
        Random rand = new Random(Settings.SEED ^ (chunkX * 0x9E3779B1));
        int tunnels = 1 + rand.nextInt(3);
        for (int i = 0; i < tunnels; i++) {
            carveTunnel(rand);
        }
    }

    private void carveTunnel(Random rand) {
        // Start innerhalb des Chunks
        double posX  = rand.nextDouble() * WIDTH;
        double posY  = rand.nextDouble() * (HEIGHT - 10) + 5;
        double angle = rand.nextDouble() * Math.PI * 2;
        double radius = 2 + rand.nextDouble() * 4; // Radius 2–6 → Dicke 4–12
        int length = 50 + rand.nextInt(100);       // 50–150 Schritte

        for (int step = 0; step < length; step++) {
            carveCircle(posX, posY, radius);

            // Mit kleiner Chance zum Surface Break
            int ix = (int) posX;
            if (ix >= 0 && ix < WIDTH) {
                int surfY = findSurfaceY(ix);
                if (posY + radius >= surfY && rand.nextDouble() < 0.05) {
                    carveCircle(posX, posY, radius);
                    break;
                }
            }

            // Pfad- und Größen-Jitter
            posX  += Math.cos(angle);
            posY  += Math.sin(angle);
            if (posX < 0 || posX >= WIDTH || posY < 5 || posY >= HEIGHT - 5) break;

            angle  += (rand.nextDouble() - 0.5) * 0.3;
            radius += (rand.nextDouble() - 0.5) * 0.5;
            radius = Math.max(2, Math.min(6, radius));
        }
    }

    private void carveCircle(double cx, double cy, double r) {
        int minX = (int) Math.floor(cx - r);
        int maxX = (int) Math.ceil(cx + r);
        int minY = (int) Math.floor(cy - r);
        int maxY = (int) Math.ceil(cy + r);

        for (int x = minX; x <= maxX; x++) {
            if (x < 0 || x >= WIDTH) continue;
            for (int y = minY; y <= maxY; y++) {
                if (y < 0 || y >= HEIGHT) continue;
                double dx = (x + 0.5) - cx;
                double dy = (y + 0.5) - cy;
                if (dx*dx + dy*dy <= r*r) {
                    blocks[x][y] = BlockType.AIR;
                }
            }
        }
    }

    private int findSurfaceY(int x) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            BlockType b = blocks[x][y];
            if (b != BlockType.AIR && b != BlockType.WATER) return y;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chunk[x=").append(chunkX).append("]\n");
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(blocks[x][y].name().charAt(0));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
