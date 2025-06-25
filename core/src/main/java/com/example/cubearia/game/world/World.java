package com.example.cubearia.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;
import com.example.cubearia.game.entities.FallingBlock;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.entities.Sheep;
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

    // Mob spawning system
    private float mobSpawnTimer = 0f;
    private final Random random = new Random();

    /**
     * Gibt den Blocktyp an globalen Koordinaten zurück.
     */
    public BlockType getBlock(int globalX, int y) {
        return getBlock(globalX, y, 0); // Default to front layer for backward compatibility
    }

    /**
     * Gibt den Blocktyp an globalen Koordinaten in einem spezifischen Layer zurück.
     */
    public BlockType getBlock(int globalX, int y, int layer) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT || layer < 0 || layer >= Chunk.LAYERS) {
            return BlockType.AIR;
        }
        return chunk.blocks[localX][y][layer];
    }

    /**
     * Setzt einen Block ohne weitere Logik (direkt im Chunk).
     */
    public void setBlock(int globalX, int y, BlockType type) {
        setBlock(globalX, y, 0, type); // Default to front layer for backward compatibility
    }

    /**
     * Setzt einen Block in einem spezifischen Layer ohne weitere Logik (direkt im Chunk).
     */
    public void setBlock(int globalX, int y, int layer, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        if (y < 0 || y >= Chunk.HEIGHT || layer < 0 || layer >= Chunk.LAYERS) return;
        getOrCreateChunk(chunkX).blocks[localX][y][layer] = type;
    }

    /**
     * Gibt den vordersten sichtbaren Block an den gegebenen Koordinaten zurück.
     * Prüft zuerst Layer 0, dann Layer 1.
     */
    public BlockType getTopBlock(int globalX, int y) {
        BlockType frontLayer = getBlock(globalX, y, 0);
        if (frontLayer != BlockType.AIR) {
            return frontLayer;
        }
        return getBlock(globalX, y, 1);
    }

    /**
     * Baut den vordersten Block an den gegebenen Koordinaten ab.
     * Prüft zuerst Layer 0, dann Layer 1.
     */
    public BlockType breakTopBlock(int globalX, int y) {
        BlockType frontLayer = getBlock(globalX, y, 0);
        if (frontLayer != BlockType.AIR) {
            setBlock(globalX, y, 0, BlockType.AIR);
            return frontLayer;
        }

        BlockType backLayer = getBlock(globalX, y, 1);
        if (backLayer != BlockType.AIR) {
            setBlock(globalX, y, 1, BlockType.AIR);
            return backLayer;
        }

        return BlockType.AIR;
    }

    /**
     * Platziert einen Block im gewünschten Layer.
     * Flüssigkeiten werden nur in Layer 1 platziert.
     * Beide Layer können unabhängig voneinander gefüllt werden.
     */
    public boolean placeBlock(int globalX, int y, BlockType type, int preferredLayer) {
        // Prüfe Kollision mit Entities vor dem Platzieren
        if (wouldBlockCollideWithEntity(globalX, y, type, preferredLayer)) {
            return false;
        }

        // Flüssigkeiten dürfen nur in Layer 1 (hinterer Layer) platziert werden
        if (type.isFluid()) {
            setBlock(globalX, y, 1, type);
            return true;
        }

        // Alle anderen Blöcke können in beiden Layern platziert werden
        // Platziere direkt im gewünschten Layer, unabhängig von anderen Layern
        setBlock(globalX, y, preferredLayer, type);
        return true;
    }

    /**
     * Prüft, ob ein Block an den gegebenen Koordinaten mit einer Entity kollidieren würde.
     * Diese Methode simuliert die Block-Platzierung und prüft Kollisionen mit allen Entities.
     */
    private boolean wouldBlockCollideWithEntity(int globalX, int y, BlockType type, int layer) {
        // Erstelle ein temporäres Rechteck für den zu platzierenden Block
        Rectangle blockBounds = new Rectangle(globalX, y, 1, 1);

        // Prüfe Kollision mit allen Entities
        for (Entity entity : entities) {
            if (entity.getBounds().overlaps(blockBounds)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update der Welt: Entities updaten, Partikel updaten und geplante Blockupdates ausführen.
     */
    public void update(float delta) {
        // Find all players
        List<Player> players = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof Player) {
                players.add((Player) e);
            }
        }

        // Update all entities with render distance tracking
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            e.update(delta);

            // Update render distance tracking against all players
            e.updateRenderDistanceTracking(delta, players);

            if (e.isRemoved()) it.remove();
        }
        updateParticles(delta);
        processScheduledBlockUpdates();
        currentTick++;

        // Mob spawning logic
        updateMobSpawning(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if (!players.isEmpty()) {
                Player player = players.get(0);
                FallingBlock fallingBlock = new FallingBlock(this, player.getX(), player.getY() + 10, BlockType.DIRT, 0, 0);
                this.addEntity(fallingBlock);
                return;
            }
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

        for (int layerIndex = 0; layerIndex < map.getLayers().getCount(); layerIndex++) {
            MapLayer mapLayer = map.getLayers().get(layerIndex);
            if (!(mapLayer instanceof TiledMapTileLayer)) continue;

            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;

            // Strukturen wie Bäume sollten in Layer 1 (hinterer Layer) platziert werden
            // damit der Spieler nicht mit ihnen kollidiert
            int targetLayer = 1; // Standard: Layer 1 für Strukturen

            for (int x = 0, w = layer.getWidth(); x < w; x++) {
                for (int y = 0, h = layer.getHeight(); y < h; y++) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        BlockType block = BlockType.getById(cell.getTile().getId() - 1);

                        // Flüssigkeiten müssen in Layer 1, unabhängig von TMX-Layer
                        if (block.isFluid()) {
                            targetLayer = 1;
                        }

                        // Nur platzieren, wenn der Ziel-Layer frei ist
                        if (getBlock(startX + x, startY + y, targetLayer) == BlockType.AIR) {
                            setBlock(startX + x, startY + y, targetLayer, block);
                        }
                    }
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

    /**
     * Updates mob spawning logic - attempts to spawn mobs around the player
     */
    private void updateMobSpawning(float delta) {
        mobSpawnTimer += delta;

        // Calculate how many spawn attempts we should make this frame
        float spawnInterval = 1f / Settings.MOB_SPAWN_RATE_PER_SECOND;
        int spawnAttempts = (int) (mobSpawnTimer / spawnInterval);

        if (spawnAttempts > 0) {
            mobSpawnTimer -= spawnInterval * spawnAttempts;

            // Find the player
            Player player = null;
            for (Entity e : entities) {
                if (e instanceof Player) {
                    player = (Player) e;
                    break;
                }
            }

            if (player != null) {
                // Check if we have too many mobs near the player
                int mobsNearPlayer = countMobsNearPlayer(player);

                if (mobsNearPlayer < Settings.MAX_MOBS_NEAR_PLAYER) {
                    // Calculate exponential spawn probability based on current mob count
                    float spawnProbability = calculateExponentialSpawnProbability(mobsNearPlayer);

                    // Try to spawn mobs with reduced probability
                    for (int i = 0; i < spawnAttempts; i++) {
                        if (random.nextFloat() < spawnProbability) {
                            attemptMobSpawn(player);
                        }
                    }
                }
            }
        }
    }

    /**
     * Berechnet die exponentielle Spawn-Wahrscheinlichkeit basierend auf der aktuellen Mob-Anzahl
     * Je näher an der maximalen Anzahl, desto unwahrscheinlicher wird das Spawnen
     */
    private float calculateExponentialSpawnProbability(int currentMobs) {
        if (currentMobs >= Settings.MAX_MOBS_NEAR_PLAYER) {
            return 0f; // Keine Spawns bei maximaler Anzahl
        }

        // Berechne den Füllstand (0.0 = leer, 1.0 = voll)
        float fillRatio = (float) currentMobs / Settings.MAX_MOBS_NEAR_PLAYER;

        // Exponentielle Abnahme: e^(-3 * fillRatio)
        // Bei 0 Mobs: Wahrscheinlichkeit = 1.0 (100%)
        // Bei 50% der Max-Anzahl: Wahrscheinlichkeit ≈ 0.22 (22%)
        // Bei 80% der Max-Anzahl: Wahrscheinlichkeit ≈ 0.09 (9%)
        // Bei 100% der Max-Anzahl: Wahrscheinlichkeit = 0.0 (0%)
        float probability = (float) Math.exp(-3.0 * fillRatio);

        return probability;
    }

    /**
     * Counts how many mobs are within the spawn radius of the player
     */
    private int countMobsNearPlayer(Player player) {
        int count = 0;
        float playerX = player.getX();
        float playerY = player.getY();
        int spawnRadius = Settings.MOB_SPAWN_RADIUS;

        for (Entity e : entities) {
            if (e instanceof Sheep) { // Currently only sheep, can be expanded
                float dx = e.getX() - playerX;
                float dy = e.getY() - playerY;
                float distanceSquared = dx * dx + dy * dy;

                if (distanceSquared <= spawnRadius * spawnRadius) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Attempts to spawn a mob at a random position around the player
     */
    private void attemptMobSpawn(Player player) {
        float playerX = player.getX();
        float playerY = player.getY();
        int spawnRadius = Settings.MOB_SPAWN_RADIUS;

        // Try multiple random positions
        for (int attempts = 0; attempts < 10; attempts++) {
            // Random position within spawn radius
            float angle = random.nextFloat() * 2f * (float) Math.PI;
            float distance = random.nextFloat() * spawnRadius;
            int spawnX = (int) (playerX + Math.cos(angle) * distance);
            int spawnY = (int) (playerY + random.nextFloat() * 10); // Random height within 10 blocks

            // Check if this is a valid spawn position
            if (isValidMobSpawnPosition(spawnX, spawnY)) {
                // Spawn a sheep (currently the only mob type)
                Sheep sheep = new Sheep(this, player, true); // true = has AI
                sheep.setPosition(spawnX, spawnY);
                addEntity(sheep);
                return; // Successfully spawned, exit
            }
        }
    }

    /**
     * Checks if a position is valid for mob spawning
     * Requirements: air block at spawn position, solid block below (only in layer 0)
     */
    private boolean isValidMobSpawnPosition(int x, int y) {
        // Check if spawn position is air (layer 0 only, since entities only collide with layer 0)
        if (getBlock(x, y, 0) != BlockType.AIR) {
            return false;
        }

        // Check if there's a solid block below (layer 0 only)
        if (getBlock(x, y - 1, 0) != BlockType.GRASSBLOCK) {
            return false;
        }

        // Check if there's enough space above (at least 2 blocks of air)
        if (getBlock(x, y + 1, 0) != BlockType.AIR) {
            return false;
        }

        // Check if no other entity is at this position
        for (Entity e : entities) {
            if (e instanceof Sheep) {
                float dx = e.getX() - x;
                float dy = e.getY() - y;
                float distanceSquared = dx * dx + dy * dy;

                if (distanceSquared < 1f) { // At least 1 block distance from other mobs
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Finds a safe spawn position for the player
     * Returns the Y coordinate where the player should spawn
     */
    public int findSafeSpawnPosition(int globalX) {
        // Start from a reasonable height and work our way down
        int startY = Math.min(Chunk.HEIGHT - 10, Settings.SEA_LEVEL + 50);

        for (int y = startY; y >= 0; y--) {
            // Check if there's a solid block at this Y level (layer 0 only for collision)
            if (getBlock(globalX, y, 0).isSolid()) {
                // Found a solid block, spawn 2 blocks above it
                return y + 2;
            }
        }

        // Fallback: spawn at sea level + 2
        return Settings.SEA_LEVEL + 2;
    }
}
