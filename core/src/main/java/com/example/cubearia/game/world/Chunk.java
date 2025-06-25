package com.example.cubearia.game.world;

import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.FallingBlock;

import java.util.*;

/**
 * Ein Chunk repräsentiert einen Abschnitt der Welt mit Terrain, Strukturen und Höhlensystemen.
 */
public class Chunk {
    public static final int WIDTH  = 32;
    public static final int HEIGHT = 512;
    public static final int LAYERS = 2;

    // 2.5D-Blöcke mit Layer-System: [x][y][layer]
    // Layer 0 = Vorderer Layer (Strukturen, Spieler-Platzierungen)
    // Layer 1 = Hinterer Layer (Terrain, Flüssigkeiten)
    public final BlockType[][][] blocks = new BlockType[WIDTH][HEIGHT][LAYERS];
    private final World world;
    private final int chunkX;
    private final BiomeType biome;
    private static final SimplexNoise NOISE = new SimplexNoise(Settings.SEED);

    /** Unbearbeitete Terrainhöhen für Höhlengeneration */
    private final int[] rawHeights = new int[WIDTH];

    public Chunk(int chunkX, BiomeType prevBiome, BiomeType biome, BiomeType nextBiome, World world) {
        this.chunkX = chunkX;
        this.world  = world;
        this.biome  = biome;

        generateTerrain(prevBiome, biome, nextBiome);
        carveCaves();
        generateLargeCaveSystems();
        removeSmallCavesPredictive(100);
    }

    public void updateNeighbors(int x, int y, int[][] dirs) {
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            int globalX = chunkX * WIDTH + nx;

            int chunkXTarget = Math.floorDiv(globalX, WIDTH);
            int localX = Math.floorMod(globalX, WIDTH);

            world.getOrCreateChunk(chunkXTarget).updateBlock(localX, ny);
        }
    }

    public void updateBlock(int x, int y) {
        // Falls Y außerhalb liegt, nichts tun
        if (y < 0 || y >= Chunk.HEIGHT) return;

        // Berechne globale X‑Koordinate dieses Blocks
        int globalX = chunkX * Chunk.WIDTH + x;

        // Prüfe beide Layer für Blockupdates
        BlockType currentLayer0 = world.getBlock(globalX, y, 0);
        BlockType currentLayer1 = world.getBlock(globalX, y, 1);

        // --- 1) Wasser‑Logik (nur in Layer 1) --------------------------------------------------

        if (currentLayer1 == BlockType.WATER) {
            // 1a) Versuch: Wasser nach unten fließen lassen
            int belowY = y - 1;
            BlockType belowLayer0 = world.getBlock(globalX, belowY, 0);
            BlockType belowLayer1 = world.getBlock(globalX, belowY, 1);

            if (belowY >= 0 && !belowLayer0.isSolid() && (belowLayer1 == BlockType.AIR || belowLayer1 == BlockType.WATER)) {
                // Setze Wasser eine Y‑Stufe tiefer
                world.setBlock(globalX, belowY, 1, BlockType.WATER);
                // Update für den neuen Wasserblock anstoßen
                world.scheduleBlockUpdate(globalX, belowY, 1f, false);
            } else {
                // 1b) Sonst: Wasser nach links/rechts ausbreiten
                int[][] sideDirs = { { -1, 0 }, { +1, 0 } };
                for (int[] d : sideDirs) {
                    int nxGlobal = globalX + d[0];
                    int ny = y; // gleiche Y‑Stufe

                    // Prüfe beide Layer - Wasser soll nur spreaden, wenn BEIDE Layer Luft sind
                    BlockType targetLayer0 = world.getBlock(nxGlobal, ny, 0);
                    BlockType targetLayer1 = world.getBlock(nxGlobal, ny, 1);

                    // Wasser spreadet nur, wenn beide Layer Luft sind
                    if (targetLayer0 == BlockType.AIR && targetLayer1 == BlockType.AIR) {
                        world.setBlock(nxGlobal, ny, 1, BlockType.WATER);
                        world.scheduleBlockUpdate(nxGlobal, ny, 1f, false);
                    }
                }
            }
        }

        // --- 2) Schwerkraft‑Logik (Beispiel: Sand) -----------------------------

        // Prüfe Sand in beiden Layern
        if (currentLayer0 == BlockType.SAND) {
            int belowY = y - 1;
            BlockType belowLayer0 = world.getBlock(globalX, belowY, 0);
            BlockType belowLayer1 = world.getBlock(globalX, belowY, 1);

            // Wenn unterhalb in beiden Layern Luft ist, Sand fallen lassen
            if (belowY >= 0 && !belowLayer0.isSolid() && !belowLayer1.isSolid()) {
                // Alten Sandblock (im Chunk) auf AIR setzen
                world.setBlock(globalX, y, 0, BlockType.AIR);

                // Eine neue FallingBlock‑Entität erzeugen (verwendet globale X‑Koordinate)
                FallingBlock fallingBlock = new FallingBlock(
                    world,
                    globalX,
                    y,
                    BlockType.SAND,
                    0, // initialer X‑Geschwindigkeitsanteil
                    0  // initialer Y‑Geschwindigkeitsanteil
                );
                world.addEntity(fallingBlock);
                world.scheduleBlockUpdate(globalX, y + 1, 0f, false);
            }
        }

        if (currentLayer1 == BlockType.SAND) {
            int belowY = y - 1;
            BlockType belowLayer0 = world.getBlock(globalX, belowY, 0);
            BlockType belowLayer1 = world.getBlock(globalX, belowY, 1);

            // Wenn unterhalb in beiden Layern Luft ist, Sand fallen lassen
            if (belowY >= 0 && !belowLayer0.isSolid() && !belowLayer1.isSolid()) {
                // Alten Sandblock (im Chunk) auf AIR setzen
                world.setBlock(globalX, y, 1, BlockType.AIR);

                // Eine neue FallingBlock‑Entität erzeugen (verwendet globale X‑Koordinate)
                FallingBlock fallingBlock = new FallingBlock(
                    world,
                    globalX,
                    y,
                    BlockType.SAND,
                    0, // initialer X‑Geschwindigkeitsanteil
                    0  // initialer Y‑Geschwindigkeitsanteil
                );
                world.addEntity(fallingBlock);
                world.scheduleBlockUpdate(globalX, y + 1, 0f, false);
            }
        }
    }

    /**
     * Erzeugt das Terrain, zeichnet Strukturen und sichert Roh-Höhen.
     */
    private void generateTerrain(BiomeType prev, BiomeType biome, BiomeType next) {
        int[] raw = Settings.FLATWORLD
            ? getFlatHeights()
            : getRawHeights(chunkX, biome);
        System.arraycopy(raw, 0, rawHeights, 0, WIDTH);

        if (!Settings.FLATWORLD) {
            int[] heights = raw.clone();
            smoothEdges(raw, heights, prev, biome, next);
            raw = heights;
        }

        // Erste Phase: Generiere Terrain und Wasser
        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            for (int y = 0; y < HEIGHT; y++) {
                BlockType type;
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    BiomeType.Layer layer = biome.getLayerAtDepth(depth);
                    type = layer.block;
                } else if (y == Settings.SEA_LEVEL) {
                    type = (biome == BiomeType.COLD_OCEAN || biome == BiomeType.COLD_SHALLOW_OCEAN)
                        ? BlockType.ICE_BLOCK : BlockType.WATER;
                } else if (y < Settings.SEA_LEVEL) {
                    type = BlockType.WATER;
                } else {
                    type = BlockType.AIR;
                }

                // Konsistente Layer-Platzierung basierend auf Biome-Definitionen:
                // Verwende die targetLayer Werte aus den Biome-Definitionen
                int targetLayer = 1; // Standard: hinterer Layer

                if (type.isFluid()) {
                    targetLayer = 1; // Flüssigkeiten immer in Layer 1
                } else if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    BiomeType.Layer layer = biome.getLayerAtDepth(depth);
                    targetLayer = layer.targetLayer; // Verwende targetLayer aus Biome-Definition
                }

                // Spezialbehandlung für targetLayer = -1: Fülle beide Layer
                if (targetLayer == -1) {
                    blocks[x][y][0] = type; // Fülle Layer 0
                    blocks[x][y][1] = type; // Fülle Layer 1
                } else {
                    // Normale Layer-Platzierung
                    blocks[x][y][targetLayer] = type;
                    // Anderer Layer ist standardmäßig Luft
                    blocks[x][y][1 - targetLayer] = BlockType.AIR;
                }
            }
            // Bedrock nur ganz unten in Layer 1
            blocks[x][0][0] = BlockType.BEDROCK;
        }

        // Zweite Phase: Generiere Strukturen (nach dem Terrain/Wasser)
        if (Settings.STRUCTURES) {
            for (int x = 0; x < WIDTH; x++) {
                int terrainHeight = raw[x];
                // Bei Ozeanen: Generiere Strukturen am Meeresgrund (terrainHeight - 1)
                // Bei anderen Biomen: Generiere Strukturen an der Oberfläche (terrainHeight - 1)
                generateStructures(chunkX * WIDTH + x, terrainHeight - 1, biome);
            }
        }

        // Generiere Erze nach der Terrain-Generierung
        generateOres();
    }

    private int[] getFlatHeights() {
        int[] h = new int[WIDTH];
        Arrays.fill(h, Settings.SEA_LEVEL + 2);
        return h;
    }

    private int[] getRawHeights(int chunkX, BiomeType biome) {
        int[] raw = new int[WIDTH];
        for (int x = 0; x < WIDTH; x++) {
            int gx = chunkX * WIDTH + x;
            int base  = (int) Noise.getHeight(gx, biome.getFluctuation());
            int water = biome.getWaterDepth();
            raw[x] = water > 0 ? base - water + (HEIGHT - 128) : base + (HEIGHT - 128);
        }
        return raw;
    }

    private void smoothEdges(int[] raw, int[] heights, BiomeType prev, BiomeType b, BiomeType next) {
        if (b.name().contains("OZEAN")) return;
        int blend = 8;
        if (prev != null) blendSide(raw, heights, prev, blend, true);
        if (next != null) blendSide(raw, heights, next, blend, false);
    }

    private void blendSide(int[] raw, int[] heights, BiomeType o, int blend, boolean left) {
        int[] other = getRawHeights(chunkX + (left ? -1 : 1), o);
        float ih = left ? (other[WIDTH-1] + raw[0]) / 2f : (other[0] + raw[WIDTH-1]) / 2f;
        for (int i = 0; i < blend; i++) {
            float t = 1 - (i + 1) / (float)(blend + 1);
            int idx = left ? i : WIDTH-1-i;
            heights[idx] = (int)(raw[idx] * (1-t) + ih * t);
        }
    }

    public void generateStructures(int globalX, int y, BiomeType biome) {
        for (Structure s : biome.getStructures()) {
            long seed = computeStructureSeed(globalX, y, s);
            float randomValue = new Random(seed).nextFloat();
            if (randomValue < s.getSpawnProbability()) {
                int finalX = globalX - s.getTrunkXOffset();
                int finalY = y + s.getTrunkYOffset();
                world.queueStructure(s.getFilePath(), finalX, finalY, s.getMinDistance());
            }
        }
    }

    private long computeStructureSeed(int x, int y, Structure s) {
        long seed = Settings.SEED;
        seed = seed * 31 + x;
        seed = seed * 31 + y;
        seed = seed * 31 + s.getFilePath().hashCode();
        return seed;
    }

    private void carveCaves() {
        double scale = 0.05;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int depth = rawHeights[x] - y - 1;
                if (depth >= 10) {
                    // Prüfe nur Layer 0 für Stein (Vorderer Layer für Höhlen)
                    boolean hasStone = blocks[x][y][0] == BlockType.STONE;

                    if (hasStone) {
                        double v = (NOISE.eval((chunkX*WIDTH + x)*scale, y*scale) + 1) * 0.45;
                        // über dem Meeresspiegel fester Schwellenwert
                        double threshold = y > Settings.SEA_LEVEL ? 0.2 : dynamicThreshold(y);
                        if (v < threshold) {
                            // Höhle nur in Layer 0 erstellen (Layer 1 bleibt unverändert für Erze)
                            blocks[x][y][0] = BlockType.AIR;
                        }
                    }
                }
            }
        }
    }

    private void removeSmallCavesPredictive(int minSize) {
        boolean[][] vis = new boolean[WIDTH][HEIGHT];
        Deque<int[]> stack = new ArrayDeque<>();
        double scale = 0.05;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                // Prüfe nur Layer 0 für Höhlen (da Höhlen nur dort gecarvet werden)
                boolean hasAir = blocks[x][y][0] == BlockType.AIR;

                if (hasAir && !vis[x][y]) {
                    List<int[]> region = new ArrayList<>();
                    boolean leftCont = false, rightCont = false;
                    stack.push(new int[]{x, y}); vis[x][y] = true;
                    while (!stack.isEmpty()) {
                        int[] p = stack.pop(); region.add(p);
                        int gx = chunkX * WIDTH + p[0];
                        if (p[0] == 0 && (NOISE.eval((gx-1)*scale, p[1]*scale) + 1) * 0.5 < dynamicThreshold(p[1])) leftCont = true;
                        if (p[0] == WIDTH-1 && (NOISE.eval((gx+1)*scale, p[1]*scale) + 1) * 0.5 < dynamicThreshold(p[1])) rightCont = true;
                        for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                            int nx = p[0] + d[0], ny = p[1] + d[1];
                            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                                boolean neighborHasAir = blocks[nx][ny][0] == BlockType.AIR;
                                if (neighborHasAir && !vis[nx][ny]) {
                                    vis[nx][ny] = true; stack.push(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                    if (region.size() < minSize && !leftCont && !rightCont) {
                        for (int[] p : region) {
                            int sY = getSurfaceHeightAt(p[0]);
                            BlockType fill = BlockType.STONE;
                            // Fülle nur Layer 0 auf, wo er vorher Luft war (Layer 1 bleibt unverändert für Erze)
                            if (blocks[p[0]][p[1]][0] == BlockType.AIR) {
                                blocks[p[0]][p[1]][0] = fill;
                            }
                        }
                    }
                }
            }
        }
    }

    private double dynamicThreshold(int y) {
        if (y < 5) return lerp(0.05, 0.45, y/4.0);
        double t = y / (double)(HEIGHT - 1);
        return 0.45 * Math.pow(0.35/0.45, t);
    }

    private double lerp(double a, double b, double t) { return a + (b - a) * t; }

    private int getSurfaceHeightAt(int x) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (blocks[x][y][1] != BlockType.AIR) return y + 1;
        }
        return 0;
    }

    /**
     * Generiert Erzvorkommen in Layer 1 basierend auf VainType-Definitionen.
     */
    private void generateOres() {
        Random random = new Random(Settings.SEED + chunkX * 31L);

        for (VainType vainType : VainType.values()) {
            int attempts = 15; // Erhöht von 3 auf 15 für bessere Erzgenerierung
            for (int attempt = 0; attempt < attempts; attempt++) {
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(Chunk.HEIGHT);
                if (!vainType.canSpawnAtHeight(y)) {
                    continue;
                }
                if (blocks[x][y][1] != BlockType.STONE) {
                    continue;
                }
                if (random.nextFloat() > vainType.getSpawnProbability()) {
                    continue;
                }
                int size = random.nextInt(vainType.getMaxSize() - vainType.getMinSize() + 1) + vainType.getMinSize();
                generateOreVein(x, y, vainType, size, random);
            }
        }
    }

    /**
     * Generiert ein einzelnes Erzvorkommen um die gegebene Position.
     */
    private void generateOreVein(int centerX, int centerY, VainType vainType, int size, Random random) {
        BlockType oreBlock = vainType.getOreBlock();

        // Generiere das Vorkommen in einer unregelmäßigen Form
        for (int i = 0; i < size; i++) {
            // Zufällige Offset-Position um das Zentrum
            int offsetX = random.nextInt(5) - 2; // -2 bis +2
            int offsetY = random.nextInt(5) - 2; // -2 bis +2

            int x = centerX + offsetX;
            int y = centerY + offsetY;

            // Prüfe Grenzen
            if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
                continue;
            }

            // Prüfe, ob an dieser Position Stone ist (nur in Layer 1)
            if (blocks[x][y][1] == BlockType.STONE) {
                // Ersetze Stone durch Erz in Layer 1
                blocks[x][y][1] = oreBlock;
            }
        }
    }

    /**
     * Generiert große, komplexe Höhlensysteme mit natürlichen, verzweigten Formen.
     * Diese Höhlen sind viel größer als normale Höhlen und können über mehrere Chunks gehen.
     * Verwendet Noise-basierte, vorhersagbare Generierung mit Übergängen zwischen Höhlen.
     */
    private void generateLargeCaveSystems() {
        // Verwende verschiedene Noise-Frequenzen für große Höhlen
        double largeCaveScale = 0.01; // Sehr niedrige Frequenz für große Höhlen
        double mediumCaveScale = 0.02; // Mittlere Frequenz für Verzweigungen
        double smallCaveScale = 0.06; // Höhere Frequenz für Details
        double connectionScale = 0.04; // Frequenz für Höhlenverbindungen

        // Erste Phase: Generiere die Haupthöhlen
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int globalX = chunkX * WIDTH + x;
                double largeCaveNoise = (NOISE.eval(globalX * largeCaveScale, y * largeCaveScale) + 1) * 0.5;
                double mediumCaveNoise = (NOISE.eval(globalX * mediumCaveScale, y * mediumCaveScale) + 1) * 0.5;
                double smallCaveNoise = (NOISE.eval(globalX * smallCaveScale, y * smallCaveScale) + 1) * 0.5;
                double caveValue = (largeCaveNoise * 0.6 + mediumCaveNoise * 0.3 + smallCaveNoise * 0.1);

                // Korrekte Tiefenberechnung (großer Faktor = tief, kleiner Faktor = hoch)
                float depthFactor = (HEIGHT - y) / (float)HEIGHT;
                // Deutlich höherer Schwellenwert für viel weniger Höhlen
                float threshold = 0.78f - (float)Math.pow(depthFactor, 2) * 0.22f;

                if (caveValue > threshold) {
                    float caveSize = getCaveSize(y, largeCaveNoise, mediumCaveNoise);

                    boolean canCarveAll = false;
                    if (y > Settings.SEA_LEVEL - 30) { // Nur nahe der Oberfläche
                        double surfaceBreakNoise = (NOISE.eval(globalX * 0.1, y * 0.1) + 1) * 0.5;
                        if (surfaceBreakNoise > 0.8) { // Reduziert auf 5%
                            canCarveAll = true;
                        }
                    }
                    generateNoiseBasedCaveCrossSection(x, y, caveSize, largeCaveNoise, mediumCaveNoise, smallCaveNoise, canCarveAll);
                }
            }
        }

        // Zweite Phase: Generiere Verbindungen
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int globalX = chunkX * WIDTH + x;

                if (blocks[x][y][0] == BlockType.AIR) continue;

                double connectionNoise = (NOISE.eval(globalX * connectionScale, y * connectionScale) + 1) * 0.5;

                if (connectionNoise < 0.20) { // Reduziert auf 20%
                    boolean nearbyCave = false;
                    float minDistance = Float.MAX_VALUE;

                    for (int searchX = Math.max(0, x - 8); searchX <= Math.min(WIDTH - 1, x + 8); searchX++) {
                        for (int searchY = Math.max(0, y - 8); searchY <= Math.min(HEIGHT - 1, y + 8); searchY++) {
                            if (blocks[searchX][searchY][0] == BlockType.AIR) {
                                float distance = (float) Math.sqrt((x - searchX) * (x - searchX) + (y - searchY) * (y - searchY));
                                if (distance < minDistance) minDistance = distance;
                                if (distance < 6f) {
                                    nearbyCave = true;
                                    break;
                                }
                            }
                        }
                        if (nearbyCave) break;
                    }

                    if (nearbyCave) {
                        boolean canCarveAll = false;
                        if (y > Settings.SEA_LEVEL - 30) { // Nur nahe der Oberfläche
                            double surfaceBreakNoise = (NOISE.eval(globalX * 0.1, y * 0.1) + 1) * 0.5;
                            if (surfaceBreakNoise > 0.95) { // Reduziert auf 5%
                                canCarveAll = true;
                            }
                        }
                        float distanceFactor = Math.max(0.5f, 1f - (minDistance / 10f));
                        generateConnectionTunnelWithParams(x, y, connectionNoise, distanceFactor, canCarveAll);
                    }
                }
            }
        }
    }

    /**
     * Berechnet die Größe einer Höhle basierend auf Tiefe und Noise.
     */
    private float getCaveSize(int y, double largeNoise, double mediumNoise) {
        // Basis-Größe wächst stark mit der Tiefe
        float depthFactor = (HEIGHT - y) / (float) HEIGHT;
        float baseSize = 1.5f + (float)Math.pow(depthFactor, 1.5) * 6.0f;

        // Variation basierend auf Noise (reduziert)
        float noiseVariation = (float)((largeNoise - 0.5) * 2.0f + (mediumNoise - 0.5) * 1.0f);

        return Math.max(1.5f, baseSize + noiseVariation);
    }

    /**
     * Generiert einen Höhlenquerschnitt basierend auf Noise-Werten.
     */
    private void generateNoiseBasedCaveCrossSection(int centerX, int centerY, float baseRadius,
                                                   double largeNoise, double mediumNoise, double smallNoise, boolean canCarveAll) {
        // Berechne effektiven Radius basierend auf Noise (weiter reduziert)
        float effectiveRadius = baseRadius + (float)((largeNoise - 0.5) * 3f + (mediumNoise - 0.5) * 1.5f);
        effectiveRadius = Math.max(1f, effectiveRadius);

        // Generiere Höhlenform um das Zentrum
        for (int dx = -(int)effectiveRadius - 3; dx <= (int)effectiveRadius + 3; dx++) {
            for (int dy = -(int)effectiveRadius - 3; dy <= (int)effectiveRadius + 3; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;

                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) continue;

                // Berechne Abstand vom Zentrum
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Verwende Noise für unregelmäßige Höhlenform
                int globalX = chunkX * WIDTH + x;
                double localNoise = (NOISE.eval(globalX * 0.1, y * 0.1) + 1) * 0.5;

                // Variiere Radius basierend auf lokaler Position (reduziert)
                float localRadius = effectiveRadius + (float)((localNoise - 0.5) * 1.0f);

                // Erstelle Höhle mit weichem Übergang
                if (distance < localRadius) {
                    float probability = 1.0f - (distance / localRadius);
                    probability = Math.max(0, probability);

                    // Zusätzliche Variation durch Noise (reduziert)
                    probability += (float)((smallNoise - 0.5) * 0.15f);

                    BlockType currentBlock = blocks[x][y][0];
                    // Oberflächen-Durchbrüche dürfen nur natürliche Blöcke entfernen, keine Strukturen.
                    boolean isNatural = currentBlock == BlockType.STONE || currentBlock == BlockType.DIRT || currentBlock == BlockType.GRASSBLOCK || currentBlock == BlockType.SAND;
                    boolean shouldCarve = (canCarveAll && isNatural) || currentBlock == BlockType.STONE;

                    // Entferne Block mit Wahrscheinlichkeit
                    if (probability > 0.5f && shouldCarve) {
                        blocks[x][y][0] = BlockType.AIR;
                    }
                }
            }
        }
    }

    /**
     * Generiert einen Verbindungstunnel zwischen Höhlen mit erweiterten Parametern.
     */
    private void generateConnectionTunnelWithParams(int centerX, int centerY, double connectionNoise, float distanceFactor, boolean canCarveAll) {
        // Erweiterte Parameter für Verbindungstunnel
        float baseTunnelRadius = 2.1f; // Weiter reduziert
        float noiseVariation = 1.5f; // Weniger Variation
        // Tiefenmultiplikator wird jetzt nicht-linear an die Höhlengröße angepasst
        float depthMultiplier = 1.0f + (float)Math.pow((HEIGHT - centerY) / (float)HEIGHT, 1.5) * 1.0f;

        // Verbindungstunnel sind breiter als vorher
        float tunnelRadius = baseTunnelRadius * depthMultiplier * distanceFactor + (float)(connectionNoise * noiseVariation);

        // Generiere Tunnel in verschiedene Richtungen
        for (int dx = -(int)tunnelRadius - 4; dx <= (int)tunnelRadius + 4; dx++) {
            for (int dy = -(int)tunnelRadius - 4; dy <= (int)tunnelRadius + 4; dy++) {
                int currentX = centerX + dx;
                int currentY = centerY + dy;

                if (currentX < 0 || currentX >= WIDTH || currentY < 0 || currentY >= HEIGHT) continue;

                // Berechne Abstand vom Zentrum
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Verwende Noise für unregelmäßige Tunnelform
                int globalX = chunkX * WIDTH + currentX;
                double localNoise = (NOISE.eval(globalX * 0.1, currentY * 0.1) + 1) * 0.5;

                // Variiere Tunnelradius basierend auf lokaler Position
                float localRadius = tunnelRadius + (float)((localNoise - 0.5) * 1.0f);

                // Erstelle Tunnel mit weichem Übergang
                if (distance < localRadius) {
                    float probability = 1.0f - (distance / localRadius);
                    probability = Math.max(0, probability);

                    // Zusätzliche Variation durch Noise (reduziert)
                    probability += (float)((connectionNoise - 0.5) * 0.2f);

                    BlockType currentBlock = blocks[currentX][currentY][0];
                    // Oberflächen-Durchbrüche dürfen nur natürliche Blöcke entfernen, keine Strukturen.
                    boolean isNatural = currentBlock == BlockType.STONE || currentBlock == BlockType.DIRT || currentBlock == BlockType.GRASSBLOCK || currentBlock == BlockType.SAND;
                    boolean shouldCarve = (canCarveAll && isNatural) || currentBlock == BlockType.STONE;

                    // Entferne Block mit Wahrscheinlichkeit
                    if (probability > 0.5f && shouldCarve) {
                        blocks[currentX][currentY][0] = BlockType.AIR;
                    }
                }
            }
        }
    }
}
