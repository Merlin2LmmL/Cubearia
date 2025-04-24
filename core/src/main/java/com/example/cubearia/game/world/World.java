package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;
import java.util.*;

/**
 * Die Welt verwaltet Chunks, Entities und Strukturen.
 */
public class World {
    private final Map<Integer, Chunk> chunks = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();

    // Strukturverwaltung
    private final List<PendingStructure> pendingStructures = new ArrayList<>();
    private final List<PlacedStructure> placedStructures = new ArrayList<>();
    private boolean loadingStructure = false;

    private static final int MAX_CHUNK_DISTANCE = 8;
    private final NoiseBasedBiomeGenerator biomeGenerator = new NoiseBasedBiomeGenerator();

    /**
     * Gibt den Blocktyp an globalen Koordinaten zurück.
     */
    public BlockType getBlock(int globalX, int y) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return BlockType.AIR;
        return chunk.blocks[localX][y];
    }

    /**
     * Setzt einen Block ohne weitere Logik.
     */
    public void setBlock(int globalX, int y, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return;
        chunk.blocks[localX][y] = type;
    }

    /**
     * Update der Welt: Entities updaten und Chunks verwalten.
     */
    public void update(float delta) {
        List<Entity> copy = new ArrayList<>(entities);
        for (Entity e : copy) {
            e.update(delta);
        }
        entities.removeIf(Entity::isRemoved);
    }

    /**
     * Rendert alle Entities.
     */
    public void renderEntities(SpriteBatch batch) {
        for (Entity e : entities) {
            e.render(batch);
        }
    }

    /**
     * Holt oder erzeugt einen Chunk und wendet Strukturen an.
     */
    public Chunk getOrCreateChunk(int chunkX) {
        if (!chunks.containsKey(chunkX)) {
            System.out.println(chunkX);
            int baseX = chunkX * Chunk.WIDTH;
            BiomeType left  = getBiomeTypeAt(baseX - Chunk.WIDTH);
            BiomeType curr  = getBiomeTypeAt(baseX);
            BiomeType right = getBiomeTypeAt(baseX + Chunk.WIDTH);
            Chunk chunk = new Chunk(chunkX, left, curr, right, this);
            chunks.put(chunkX, chunk);
        }
        if (!loadingStructure) {
            applyPendingStructures(chunkX);
        }
        return chunks.get(chunkX);
    }

    /**
     * Lädt die Chunks um den Spieler herum und entfernt ferne Chunks.
     */
    public void updateAroundPlayer(float playerX) {
        int center = Math.floorDiv((int)playerX, Chunk.WIDTH);
        int range = Settings.RENDERDISTANCE;
        for (int dx = -range; dx <= range; dx++) {
            getOrCreateChunk(center + dx);
        }
        chunks.keySet().removeIf(cx -> Math.abs(cx - center) > MAX_CHUNK_DISTANCE);
    }

    /**
     * Queued eine Struktur, Abstand zum nächsten prüfend.
     */
    public void queueStructure(String file, int globalX, int globalY, int minDistance) {
        if (loadingStructure) return;
        long minDistSq = (long)minDistance * minDistance;
        for (PendingStructure ps : pendingStructures) {
            if (distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq) {
                return;
            }
        }
        for (PlacedStructure ps : placedStructures) {
            if (distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq) {
                return;
            }
        }
        pendingStructures.add(new PendingStructure(file, globalX, globalY));
    }

    private void applyPendingStructures(int chunkX) {
        Iterator<PendingStructure> it = pendingStructures.iterator();
        int startX = chunkX * Chunk.WIDTH;
        int endX   = startX + Chunk.WIDTH;
        while (it.hasNext()) {
            PendingStructure ps = it.next();
            int cx = Math.floorDiv(ps.globalX, Chunk.WIDTH);
            if (cx == chunkX) {
                loadStructure(ps.file, ps.globalX, ps.globalY);
                placedStructures.add(new PlacedStructure(ps.file, ps.globalX, ps.globalY));
                it.remove();
            }
        }
    }

    private void loadStructure(String mapFile, int startX, int startY) {
        loadingStructure = true;
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(mapFile);
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    int tileId = cell.getTile().getId();
                    BlockType block = BlockType.getById(tileId - 1);
                    setBlock(startX + x, startY + y, block);
                }
            }
        }
        loadingStructure = false;
    }

    public BiomeType getBiomeTypeAt(int globalX) {
        return Settings.FLATWORLD ? BiomeType.PLAINS : biomeGenerator.getBiomeAt(globalX);
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public Map<Integer, Chunk> getChunks() {
        return Collections.unmodifiableMap(chunks);
    }

    private long distanceSquared(int x1, int y1, int x2, int y2) {
        long dx = x1 - x2;
        long dy = y1 - y2;
        return dx*dx + dy*dy;
    }

    // Helper-Klassen
    private static class PendingStructure {
        String file;
        int globalX, globalY;
        PendingStructure(String f, int gx, int gy) {
            this.file = f;
            this.globalX = gx;
            this.globalY = gy;
        }
    }

    private static class PlacedStructure {
        String file;
        int globalX, globalY;
        PlacedStructure(String f, int gx, int gy) {
            this.file = f;
            this.globalX = gx;
            this.globalY = gy;
        }
    }
}
