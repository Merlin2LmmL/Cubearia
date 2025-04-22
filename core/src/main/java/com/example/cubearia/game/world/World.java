package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class World {
    public HashMap<Integer, Chunk> chunks = new HashMap<>();
    private List<Entity> entities = new ArrayList<>();
    NoiseBasedBiomeGenerator biomeGenerator = new NoiseBasedBiomeGenerator();
    private static final int MAX_CHUNK_DISTANCE = 8;

    public BlockType getBlock(int globalX, int y) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return BlockType.AIR;
        return chunk.blocks[localX][y];
    }

    public void update(float delta) {
        // Kopie der Liste erstellen, um ConcurrentModificationException zu vermeiden
        List<Entity> copy = new ArrayList<>(entities);
        for (Entity e : copy) {
            e.update(delta);
        }

        // Entferne Entities, die sich selbst entfernt haben
        entities.removeIf(Entity::isRemoved);
    }

    public void renderEntities(SpriteBatch batch) {
        for (Entity e : entities) {
            e.render(batch);
        }
    }

    public Chunk getOrCreateChunk(int chunkX) {
        Chunk chunk = chunks.get(chunkX);
        if (chunk == null) {
            int globalX = chunkX * Chunk.WIDTH;
            BiomeType biome = getBiomeTypeAt(globalX);
            chunk = new Chunk(chunkX, getBiomeTypeAt((int) (globalX - 16)), biome, getBiomeTypeAt((int) (globalX + 16)), this);
            chunks.put(chunkX, chunk);
        }
        return chunk;
    }

    // Aktualisierung der Chunks in der Nähe des Spielers
    public void updateAroundPlayer(float playerX) {
        int centerChunk = (int)Math.floor(playerX / Chunk.WIDTH);
        for (int dx = -2 * Settings.RENDERDISTANCE; dx <= 2 * Settings.RENDERDISTANCE; dx++) {
            int cx = centerChunk + dx;
            getOrCreateChunk(cx);
        }
        // Entferne alte Chunks
        chunks.entrySet().removeIf(entry -> Math.abs(entry.getKey() - centerChunk) > MAX_CHUNK_DISTANCE);
    }

    // Methode um einen Block zu setzen (z.B. beim Abbau)
    public void setBlock(int globalX, int y, BlockType type) {
        int chunkX = Math.floorDiv(globalX, Chunk.WIDTH);
        int localX = Math.floorMod(globalX, Chunk.WIDTH);
        Chunk chunk = getOrCreateChunk(chunkX);
        if (y < 0 || y >= Chunk.HEIGHT) return;
        chunk.blocks[localX][y] = type;
    }

    // Methode zum Laden und Einfügen einer Struktur (Map) in die Welt
    public void loadStructure(String mapFile, int startX, int startY) {
        TmxMapLoader mapLoader = new TmxMapLoader();
        TiledMap map = mapLoader.load(mapFile);

        // Hole die Haupt-Schicht der Map (Die Layer, in denen die Blöcke gespeichert sind)
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0); // 0 = erstes Layer

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                // Hole die Tile-ID an der Position (x, y)
                int tileId = layer.getCell(x, y) != null ? layer.getCell(x, y).getTile().getId() : -1;

                if (tileId != -1) {
                    // Hier kannst du den Blocktyp anhand der Tile-ID bestimmen
                    BlockType block = BlockType.getById(tileId - 1);

                    // Setze den Block in die Welt
                    setBlock(startX + x, startY + y, block);
                }
            }
        }
    }

    public BiomeType getBiomeTypeAt(int globalX) {
        return biomeGenerator.getBiomeAt(globalX); // korrekt aus dem Generator holen
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
