package com.example.cubearia.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;
import com.example.cubearia.game.entities.FallingBlock;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.entities.particle.Particle;

import java.util.*;

/**
 * Die Welt verwaltet Chunks, Entities, Strukturen und Blockupdates mit optimierter Performance.
 */
public class World {
    // IntMap vermeidet Integer-Boxing und ist performanter als HashMap
    private final IntMap<Chunk> chunks = new IntMap<>();
    private final List<Entity> entities = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    // Strukturverwaltung
    private final List<PendingStructure> pendingStructures = new ArrayList<>();
    private final List<PlacedStructure> placedStructures = new ArrayList<>();
    private boolean loadingStructure = false;

    private final NoiseBasedBiomeGenerator biomeGenerator = new NoiseBasedBiomeGenerator();

    // Blockupdate-Verwaltung
    private long currentTick = 0;
    private final List<ScheduledBlockUpdate> scheduledBlockUpdates = new ArrayList<>();
    private final List<ScheduledBlockUpdate> newlyScheduledUpdates = new ArrayList<>();


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
     * Setzt einen Block ohne weitere Logik (direkt im Chunk).
     */
    public void setBlock(int globalX, int y, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        if (y < 0 || y >= Chunk.HEIGHT) return;
        getOrCreateChunk(chunkX).blocks[localX][y][1] = type;
    }

    /**
     * Update der Welt: Entities updaten, Partikel updaten und geplante Blockupdates ausführen.
     */
    public void update(float delta) {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.update(delta);
            if (e.isRemoved()) it.remove();
        }
        updateParticles(delta);
        processScheduledBlockUpdates();
        currentTick++;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            Player player = null;
            for (Entity e : entities) {
                if (e instanceof Player) {
                    player = (Player) e;
                    break;
                }
            }

                FallingBlock fallin = new FallingBlock(this, player.getX(), player.getY() + 10, BlockType.DIRT, 0, 0);
                this.addEntity(fallin);
                return;
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

    /**
     * Plant ein Block‐Update, das in 'delaySeconds' Sekunden ausgeführt werden soll.
     * Beispiel: scheduleBlockUpdate(globalX, y, 2.5f) → nach 2,5 Sekunden wird
     * in processScheduledBlockUpdates(...) das Update ausgelöst.
     */
    public void scheduleBlockUpdate(int globalX, int y, float delaySeconds, boolean surrounding) {
        long executeAtMillis = TimeUtils.millis() + (long)(delaySeconds * 1000);
        newlyScheduledUpdates.add(new ScheduledBlockUpdate(globalX, y, executeAtMillis, surrounding));
    }

    /**
     * Ein BlockUpdate ohne Verzögerung (= sofort in der nächsten Verarbeitungsschleife).
     */
    public void blockUpdate(int globalX, int y, boolean surrounding) {
        scheduleBlockUpdate(globalX, y, 0f, surrounding);
    }

    /**
     * Diese Methode wird regelmäßig (z.B. pro Frame/Tick) aufgerufen.
     * Sie durchläuft alle geplanten Updates und führt jedes aus, dessen
     * executeAtMillis ≤ aktueller Systemzeit ist.
     */
    private void processScheduledBlockUpdates() {
        long nowMillis = TimeUtils.millis();

        // Erst alle aktuellen Updates verarbeiten
        Iterator<ScheduledBlockUpdate> it = scheduledBlockUpdates.iterator();
        while (it.hasNext()) {
            ScheduledBlockUpdate s = it.next();
            if (nowMillis >= s.executeAtMillis) {
                int chunkX = Math.floorDiv(s.globalX, Chunk.WIDTH);
                int localX = Math.floorMod(s.globalX, Chunk.WIDTH);

                Chunk target = getOrCreateChunk(chunkX);
                int[][] dirs = s.surrounding
                    ? new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}
                    : new int[][]{{0, 0}};
                target.updateNeighbors(localX, s.y, dirs);

                it.remove();
            }
        }

        // Danach neue Updates hinzufügen (damit sie **nicht** im selben Frame verarbeitet werden)
        scheduledBlockUpdates.addAll(newlyScheduledUpdates);
        newlyScheduledUpdates.clear();
    }

    /**
     * Innere Klasse repräsentiert ein geplantes Update.
     * 'executeAtMillis' ist der absolute Zeitpunkt (System‐Millis), an dem
     * das Update ausgelöst werden soll.
     */
    private static class ScheduledBlockUpdate {
        final int globalX;
        final int y;
        final long executeAtMillis;
        final boolean surrounding;

        ScheduledBlockUpdate(int globalX, int y, long executeAtMillis, boolean surrounding) {
            this.globalX = globalX;
            this.y = y;
            this.executeAtMillis = executeAtMillis;
            this.surrounding = surrounding;
        }
    }

    public void spawnParticle(Particle p) {
        particles.add(p);
    }

    public void updateParticles(float delta) {
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) {
                particles.remove(i);
                i--;
            }
        }
    }

    public void renderParticles(SpriteBatch batch) {
        for (Particle p : particles) {
            p.render(batch);
        }
    }
}
