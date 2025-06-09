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
    public static final int LAYERS = 3;

    // 3D-Blöcke, Ebene 1 enthält das Terrain
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
        carveMegaCaverns(5, 30, 100, 0.3, 2);
        carveCaves();
        removeSmallCavesPredictive(100);
    }

    public void updateNeighbors(int x, int y, int[][] dirs) {
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            int globalX = chunkX * WIDTH + nx;

            int chunkXTarget = Math.floorDiv(globalX, WIDTH);
            int localX = Math.floorMod(globalX, WIDTH);

            for (int layer = 0; layer < LAYERS; layer++) {
                world.getOrCreateChunk(chunkXTarget).updateBlock(localX, ny, layer);
            }
        }
    }

    public void updateBlock(int x, int y, int layer) {
        // Falls Y außerhalb liegt, nichts tun
        if (y < 0 || y >= Chunk.HEIGHT) return;

        // Berechne globale X‑Koordinate dieses Blocks
        int globalX = chunkX * Chunk.WIDTH + x;

        // Aktuellen Blocktyp über World.getBlock abfragen
        BlockType current = world.getBlock(globalX, y);

        // --- 1) Wasser‑Logik --------------------------------------------------

        if (current == BlockType.WATER) {
            // 1a) Versuch: Wasser nach unten fließen lassen
            int belowY = y - 1;
            BlockType below = world.getBlock(globalX, belowY);

            if (belowY >= 0 && (below == BlockType.AIR || below == BlockType.WATER)) {
                // Setze Wasser eine Y‑Stufe tiefer
                world.setBlock(globalX, belowY, BlockType.WATER);
                // Update für den neuen Wasserblock anstoßen
                world.scheduleBlockUpdate(globalX, belowY, 1, false);
            } else {
                // 1b) Sonst: Wasser nach links/rechts ausbreiten
                int[][] sideDirs = { { -1, 0 }, { +1, 0 } };
                for (int[] d : sideDirs) {
                    int nxGlobal = globalX + d[0];
                    int ny = y; // gleiche Y‑Stufe

                    // Nur dann setzen, wenn darunter kein Stein existiert und aktueller Block Luft ist
                    BlockType target = world.getBlock(nxGlobal, ny);
                    if (target == BlockType.AIR) {
                        world.setBlock(nxGlobal, ny, BlockType.WATER);
                        world.scheduleBlockUpdate(nxGlobal, ny, 1, false);
                    }
                }
            }
        }

        // --- 2) Schwerkraft‑Logik (Beispiel: Sand) -----------------------------

        if (current == BlockType.SAND) {
            int belowY = y - 1;
            BlockType below = world.getBlock(globalX, belowY);

            // Wenn unterhalb Luft ist, Sand fallen lassen
            if (belowY >= 0 && !below.isSolid()) {
                // Alten Sandblock (im Chunk) auf AIR setzen
                world.setBlock(globalX, y, BlockType.AIR);

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

        for (int x = 0; x < WIDTH; x++) {
            int terrainHeight = raw[x];
            boolean surfaceDone = false;
            for (int y = 0; y < HEIGHT; y++) {
                BlockType type;
                if (y < terrainHeight) {
                    int depth = terrainHeight - y - 1;
                    type = biome.getBlockAtDepth(depth);
                    if (!surfaceDone && y == terrainHeight - 1 && Settings.STRUCTURES) {
                        generateStructures(chunkX * WIDTH + x, y, biome);
                        surfaceDone = true;
                    }
                } else if (y == Settings.SEA_LEVEL) {
                    type = (biome == BiomeType.COLD_OCEAN || biome == BiomeType.COLD_SHALLOW_OCEAN)
                        ? BlockType.ICE_BLOCK : BlockType.WATER;
                } else if (y < Settings.SEA_LEVEL) {
                    type = BlockType.WATER;
                } else {
                    type = BlockType.AIR;
                }
                blocks[x][y][1] = type;
            }
            // Bedrock nur ganz unten
            blocks[x][0][1] = BlockType.BEDROCK;
        }
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
            if (new Random(seed).nextFloat() < s.getSpawnProbability()) {
                world.queueStructure(s.getFilePath(), globalX - s.getTrunkXOffset(), y + s.getTrunkYOffset(), s.getMinDistance());
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

    private void carveMegaCaverns(int count, int minR, int maxR, double dev, int iters) {
        int maxAllowed = Math.min(WIDTH, HEIGHT) / 2 - 1;
        maxR = Math.min(maxR, maxAllowed);
        minR = Math.min(minR, maxR);
        Random rnd = new Random(Settings.SEED ^ chunkX);
        List<int[]> placed = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (int a = 0; a < 30; a++) {
                int cx = rnd.nextInt(WIDTH - 2*maxR) + maxR;
                int cy = rnd.nextInt(Settings.SEA_LEVEL - 2*maxR) + maxR;
                int r = rnd.nextInt(maxR - minR + 1) + minR;
                List<double[]> pts = new ArrayList<>();
                int k = rnd.nextInt(10) + 5;
                for (int j = 0; j < k; j++) {
                    double ang = 2*Math.PI*j/k;
                    double rad = r*(1 + (rnd.nextDouble()*2-1)*dev);
                    pts.add(new double[]{cx + Math.cos(ang)*rad*1.2, cy + Math.sin(ang)*rad*0.6});
                }
                pts = chaikin(pts, iters);
                int x0 = WIDTH, x1 = 0, y0 = HEIGHT, y1 = 0;
                for (double[] p : pts) {
                    x0 = Math.min(x0, (int)p[0]); x1 = Math.max(x1, (int)p[0]);
                    y0 = Math.min(y0, (int)p[1]); y1 = Math.max(y1, (int)p[1]);
                }
                boolean overlap = false;
                for (int[] b : placed) {
                    if (!(y1 < b[0] || y0 > b[2] || x1 < b[1] || x0 > b[3])) { overlap = true; break; }
                }
                if (overlap) continue;
                placed.add(new int[]{y0, x0, y1, x1});
                for (int xx = x0; xx <= x1; xx++) {
                    for (int yy = y0; yy <= y1; yy++) {
                        if (xx >= 0 && xx < WIDTH && yy >= 0 && yy < HEIGHT
                            && blocks[xx][yy][1] == BlockType.STONE
                            && pointInPoly(xx + 0.5, yy + 0.5, pts)) {
                            blocks[xx][yy][1] = BlockType.AIR;
                        }
                    }
                }
                break;
            }
        }
        connectRegions(placed);
    }

    private void carveCaves() {
        double scale = 0.05;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                int depth = rawHeights[x] - y - 1;
                if (depth >= 10 && blocks[x][y][1] == BlockType.STONE) {
                    double v = (NOISE.eval((chunkX*WIDTH + x)*scale, y*scale) + 1) * 0.5;
                    // über dem Meeresspiegel fester Schwellenwert
                    double threshold = y > Settings.SEA_LEVEL ? 0.3 : dynamicThreshold(y);
                    if (v < threshold) {
                        blocks[x][y][1] = BlockType.AIR;
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
                if (blocks[x][y][1] == BlockType.AIR && !vis[x][y]) {
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
                            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && blocks[nx][ny][1] == BlockType.AIR && !vis[nx][ny]) {
                                vis[nx][ny] = true; stack.push(new int[]{nx, ny});
                            }
                        }
                    }
                    if (region.size() < minSize && !leftCont && !rightCont) {
                        for (int[] p : region) {
                            int sY = getSurfaceHeightAt(p[0]);
                            int d = sY - p[1] - 1;
                            BlockType fill = d >= 0 ? biome.getBlockAtDepth(d) : BlockType.STONE;
                            blocks[p[0]][p[1]][1] = fill;
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

    private List<double[]> chaikin(List<double[]> pts, int it) {
        for (int i = 0; i < it; i++) {
            List<double[]> np = new ArrayList<>();
            for (int j = 0; j < pts.size(); j++) {
                double[] p0 = pts.get(j), p1 = pts.get((j+1)%pts.size());
                np.add(new double[]{0.75*p0[0] + 0.25*p1[0], 0.75*p0[1] + 0.25*p1[1]});
                np.add(new double[]{0.25*p0[0] + 0.75*p1[0], 0.25*p0[1] + 0.75*p1[1]});
            }
            pts = np;
        }
        return pts;
    }

    private boolean pointInPoly(double x, double y, List<double[]> poly) {
        boolean in = false;
        for (int i = 0, j = poly.size() - 1; i < poly.size(); j = i++) {
            double xi = poly.get(i)[0], yi = poly.get(i)[1];
            double xj = poly.get(j)[0], yj = poly.get(j)[1];
            if ((yi > y) != (yj > y) && x < (xj - xi)*(y - yi)/(yj - yi) + xi) in = !in;
        }
        return in;
    }

    private void connectRegions(List<int[]> centers) {
        // stub: Verbindung von Caves
    }
}
