package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;

import java.util.*;

public class World {
    private final Map<Integer, Chunk> chunks = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();

    // Wartende Strukturen (Originpunkte)
    private final List<PendingStructure> pendingStructures = new ArrayList<>();
    // Bereits platzierte Strukturen (für Abstandskontrolle)
    private final List<PlacedStructure> placedStructures = new ArrayList<>();

    private static final int MAX_CHUNK_DISTANCE = 8;
    private boolean loadingStructure = false;              // Lock-Flag während loadStructure

    private final NoiseBasedBiomeGenerator biomeGenerator = new NoiseBasedBiomeGenerator();

    // Liefert den Block an globalen Koordinaten
    public BlockType getBlock(int globalX, int y) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return BlockType.AIR;
        return chunk.blocks[localX][y];
    }

    // Setzt einen Block (öffnet ggf. neuen Chunk)
    public void setBlock(int globalX, int y, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return;
        chunk.blocks[localX][y] = type;
    }

    public void update(float delta) {
        List<Entity> copy = new ArrayList<>(entities);
        for (Entity e : copy) e.update(delta);
        entities.removeIf(Entity::isRemoved);
    }

    public void renderEntities(SpriteBatch batch) {
        for (Entity e : entities) e.render(batch);
    }

    public Chunk getOrCreateChunk(int chunkX) {
        if (!chunks.containsKey(chunkX)) {
            int globalX = chunkX * Chunk.WIDTH;
            BiomeType left  = getBiomeTypeAt(globalX - Chunk.WIDTH);
            BiomeType curr  = getBiomeTypeAt(globalX);
            BiomeType right = getBiomeTypeAt(globalX + Chunk.WIDTH);

            Chunk chunk = new Chunk(chunkX, left, curr, right, this);
            chunks.put(chunkX, chunk);
        }

        if (!loadingStructure) {
            tryApplyPendingStructures(chunkX);
            processPendingStructures(chunkX);
        }

        return chunks.get(chunkX);
    }

    public void updateAroundPlayer(float playerX) {
        int center = (int)Math.floor(playerX / Chunk.WIDTH);
        for (int dx = -2 * Settings.RENDERDISTANCE; dx <= 2 * Settings.RENDERDISTANCE; dx++) {
            getOrCreateChunk(center + dx);
        }
        chunks.entrySet().removeIf(e -> Math.abs(e.getKey() - center) > MAX_CHUNK_DISTANCE);
    }

    /**
     * Queued mit Standard-Abstand von 10 Blöcken.
     */
    public void queueStructure(String file, int globalX, int globalY) {
        queueStructure(file, globalX, globalY, 10);
    }

    /**
     * Queue eine Struktur, Abstand wird gegen alle Strukturen geprüft.
     */
    public void queueStructure(String file, int globalX, int globalY, int minDistance) {
        if (loadingStructure) return;
        long minDistSq = (long)minDistance * minDistance;
        for (PendingStructure ps : pendingStructures) {
            if (distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq) return;
        }
        for (PlacedStructure ps : placedStructures) {
            if (distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq) return;
        }
        pendingStructures.add(new PendingStructure(file, globalX, globalY));
    }

    private void tryApplyPendingStructures(int chunkX) {
        Iterator<PendingStructure> it = pendingStructures.iterator();
        while (it.hasNext()) {
            PendingStructure ps = it.next();
            if (Math.floorDiv(ps.globalX, Chunk.WIDTH) == chunkX) {
                loadStructure(ps.file, ps.globalX, ps.globalY);
                placedStructures.add(new PlacedStructure(ps.file, ps.globalX, ps.globalY));
                it.remove();
            }
        }
    }

    private void processPendingStructures(int chunkX) {
        Iterator<PendingStructure> it = pendingStructures.iterator();
        int startX = chunkX * Chunk.WIDTH;
        int endX   = startX + Chunk.WIDTH;
        while (it.hasNext()) {
            PendingStructure ps = it.next();
            // wenn Origin innerhalb dieses Chunk-Bereichs
            if (ps.globalX >= startX && ps.globalX < endX) {
                loadStructure(ps.file, ps.globalX, ps.globalY);
                placedStructures.add(new PlacedStructure(ps.file, ps.globalX, ps.globalY));
                it.remove();
            }
        }
    }

    /**
     * Lädt Struktur und schreibt Blöcke in alle betroffenen Chunks durch setBlock().
     */
    private void loadStructure(String mapFile, int startX, int startY) {
        loadingStructure = true;
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(mapFile);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    int tileId = cell.getTile().getId();
                    BlockType block = BlockType.getById(tileId - 1);
                    // setBlock erzeugt ggf. neue Chunks automatisch
                    setBlock(startX + x, startY + y, block);
                }
            }
        }
        loadingStructure = false;
    }

    public BiomeType getBiomeTypeAt(int globalX) {
        return Settings.FLATWORLD ? BiomeType.PLAINS : biomeGenerator.getBiomeAt(globalX);
    }

    public void addEntity(Entity e) { entities.add(e); }
    public List<Entity> getEntities()    { return entities; }

    private static class PendingStructure {
        String file; int globalX, globalY;
        PendingStructure(String file, int gx, int gy) { this.file = file; this.globalX = gx; this.globalY = gy; }
    }
    private static class PlacedStructure {
        String file; int globalX, globalY;
        PlacedStructure(String file, int gx, int gy) { this.file = file; this.globalX = gx; this.globalY = gy; }
    }

    private long distanceSquared(int x1, int y1, int x2, int y2) {
        long dx = x1 - x2, dy = y1 - y2;
        return dx*dx + dy*dy;
    }

    public Map<Integer, Chunk> getChunks() {
        return Collections.unmodifiableMap(chunks);
    }
}
