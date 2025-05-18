package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntArray;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;
import java.util.*;

/**
 * Die Welt verwaltet Chunks, Entities und Strukturen mit optimierter Performance.
 */
public class World {
    // IntMap vermeidet Integer-Boxing und ist performanter als HashMap
    private final IntMap<Chunk> chunks = new IntMap<>();
    private final List<Entity> entities = new ArrayList<>();

    // Strukturverwaltung
    private final List<PendingStructure> pendingStructures = new ArrayList<>();
    private final List<PlacedStructure> placedStructures = new ArrayList<>();
    private boolean loadingStructure = false;

    private final NoiseBasedBiomeGenerator biomeGenerator = new NoiseBasedBiomeGenerator();

    /**
     * Gibt den Blocktyp an globalen Koordinaten zurück.
     */
    public BlockType getBlock(int globalX, int y) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        return (y < 0 || y >= Chunk.HEIGHT) ? BlockType.AIR : chunk.blocks[localX][y][1];
    }

    /**
     * Setzt einen Block ohne weitere Logik.
     */
    public void setBlock(int globalX, int y, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        if (y < 0 || y >= Chunk.HEIGHT) return;
        getOrCreateChunk(chunkX).blocks[localX][y][1] = type;
    }

    /**
     * Update der Welt: Entities updaten und entfernen.
     */
    public void update(float delta) {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.update(delta);
            if (e.isRemoved()) it.remove();
        }
    }

    /**
     * Rendert alle Entities.
     */
    public void renderEntities(SpriteBatch batch) {
        for (Entity e : entities) e.render(batch);
    }

    /**
     * Holt oder erzeugt einen Chunk und wendet Strukturen an.
     */
    public Chunk getOrCreateChunk(int chunkX) {
        Chunk chunk = chunks.get(chunkX);
        if (chunk == null) {
            int baseX = chunkX * Chunk.WIDTH;
            BiomeType left  = getBiomeTypeAt(baseX - Chunk.WIDTH);
            BiomeType curr  = getBiomeTypeAt(baseX);
            BiomeType right = getBiomeTypeAt(baseX + Chunk.WIDTH);
            chunk = new Chunk(chunkX, left, curr, right, this);
            chunks.put(chunkX, chunk);
        }
        if (!loadingStructure) applyPendingStructures(chunkX);
        return chunk;
    }

    /**
     * Lädt die Chunks um den Spieler herum und entfernt ferne Chunks.
     */
    public void updateAroundPlayer(float playerX) {
        int center = Math.floorDiv((int)playerX, Chunk.WIDTH);
        int range  = Settings.RENDERDISTANCE;

        // Erst alle benötigten Chunks laden
        for (int dx = -range; dx <= range; dx++) {
            getOrCreateChunk(center + dx);
        }

        // Ferne Chunks entfernen (mit Puffer von 2 für flüssiges Nachladen)
        IntArray toRemove = new IntArray();
        for (IntMap.Entry<Chunk> entry : chunks.entries()) {
            int cx = entry.key;
            if (Math.abs(cx - center) > range + 2) {
                toRemove.add(cx);
            }
        }
        for (int i = 0; i < toRemove.size; i++) {
            chunks.remove(toRemove.get(i));
        }
    }

    /**
     * Queued eine Struktur, Abstand zum nächsten prüfend.
     */
    public void queueStructure(String file, int globalX, int globalY, int minDistance) {
        if (loadingStructure) return;
        long minDistSq = (long)minDistance * minDistance;
        boolean close = pendingStructures.stream().anyMatch(ps -> distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq)
            || placedStructures.stream().anyMatch(ps -> distanceSquared(ps.globalX, ps.globalY, globalX, globalY) < minDistSq);
        if (!close) pendingStructures.add(new PendingStructure(file, globalX, globalY));
    }

    private void applyPendingStructures(int chunkX) {
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

    private void loadStructure(String mapFile, int startX, int startY) {
        loadingStructure = true;
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(mapFile);
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
        for (int x = 0, w = layer.getWidth(); x < w; x++) {
            for (int y = 0, h = layer.getHeight(); y < h; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    BlockType block = BlockType.getById(cell.getTile().getId() - 1);
                    setBlock(startX + x, startY + y, block);
                }
            }
        }
        loadingStructure = false;
    }

    public BiomeType getBiomeTypeAt(int globalX) {
        return Settings.FLATWORLD ? BiomeType.PLAINS : biomeGenerator.getBiomeAt(globalX);
    }

    /**
     * Gibt alle Entities zurück (wird von DroppedItem benötigt)
     */
    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Fügt eine Entity zur Welt hinzu (Player, DroppedItem, etc.)
     */
    public void addEntity(Entity e) {
        entities.add(e);
    }

    public IntMap<Chunk> getChunks() {
        return chunks;
    }

    private long distanceSquared(int x1, int y1, int x2, int y2) {
        long dx = x1 - x2, dy = y1 - y2;
        return dx*dx + dy*dy;
    }

    // Helper-Klassen
    private static class PendingStructure {
        final String file;
        final int globalX, globalY;
        PendingStructure(String f, int gx, int gy) {
            this.file = f;
            this.globalX = gx;
            this.globalY = gy;
        }
    }

    private static class PlacedStructure {
        final String file;
        final int globalX, globalY;
        PlacedStructure(String f, int gx, int gy) {
            this.file = f;
            this.globalX = gx;
            this.globalY = gy;
        }
    }
}
