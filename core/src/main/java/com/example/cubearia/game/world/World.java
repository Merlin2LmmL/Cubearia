package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
            chunk = new Chunk(chunkX, getBiomeTypeAt((int) (globalX - 16)), biome, getBiomeTypeAt((int) (globalX + 16)));
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
