package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Player;

import java.awt.Point;
import java.util.*;

import static com.example.cubearia.game.GameScreen.TILE_SIZE;

public class Renderer {
    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};

    public static void renderWorld(World world, Player player, SpriteBatch batch) {
        Set<Point> visibleAir = new HashSet<>();  // Standard leere Menge, falls RENDERALLBLOCKS wahr ist
        if (!Settings.RENDERALLBLOCKS) {
            visibleAir = computeVisibleAir(world, player.x, player.y);
        }

        // 2. Alle geladenen Chunks rendern
        for (Map.Entry<Integer, Chunk> entry : world.getChunks().entrySet()) {
            int chunkX = entry.getKey();
            Chunk chunk = entry.getValue();

            if (chunkX >= (player.x / 16) - Settings.RENDERDISTANCE && chunkX <= (player.x / 16) + Settings.RENDERDISTANCE) {
                for (int lx = 0; lx < Chunk.WIDTH; lx++) {
                    for (int y = 0; y < Chunk.HEIGHT; y++) {
                        int gx = chunkX * Chunk.WIDTH + lx;
                        int gy = y;
                        Point p = new Point(gx, gy);
                        BlockType type = chunk.blocks[lx][y];
                        boolean isAir = type == BlockType.AIR;

                        // Alle Blöcke sind sichtbar, wenn RENDERALLBLOCKS wahr ist
                        boolean isVisible = Settings.RENDERALLBLOCKS || visibleAir.contains(p) || (!isAir && hasAdjacentVisibleAir(p, visibleAir));

                        float dstX = gx * TILE_SIZE;
                        float dstY = gy * TILE_SIZE;

                        // Sichtbare Luft überspringen, wenn RENDERALLBLOCKS wahr ist
                        if (isAir && visibleAir.contains(p)) {
                            continue; // sichtbare Luft = Hintergrund
                        }

                        if (!isAir && isVisible) {
                            TextureRegion region = type.getRegion();  // Hole die Region aus dem BlockType
                            if (region != null) {
                                batch.draw(region,
                                    dstX, dstY, TILE_SIZE, TILE_SIZE);  // Zeichne die Region an der gewünschten Position
                            }
                        } else if (!Settings.RENDERALLBLOCKS) {
                                batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }
    }

    private static final int RADIUS_IN_CHUNKS = Settings.RENDERDISTANCE;

    private static Set<Point> computeVisibleAir(World world, float playerX, float playerY) {
        Set<Point> visited = new HashSet<>();
        Deque<Point> queue = new ArrayDeque<>();
        int maxIterations = 100_000;
        int iterationCount = 0;

        int startX = (int) playerX;
        int startY = (int) playerY;

        // Berechne die Weltgrenzen, innerhalb derer gesucht werden darf
        int minX = ((startX / Chunk.WIDTH) - RADIUS_IN_CHUNKS) * Chunk.WIDTH;
        int maxX = ((startX / Chunk.WIDTH) + RADIUS_IN_CHUNKS + 1) * Chunk.WIDTH - 1;
        int minY = 0;
        int maxY = Chunk.HEIGHT - 1;

        // Starte nur, wenn Spieler in Luft steht
        if (world.getBlock(startX, startY).isSolid()) return visited;

        Point start = new Point(startX, startY);
        visited.add(start);
        queue.add(start);

        int[] dx = Renderer.DX;
        int[] dy = Renderer.DY;

        while (!queue.isEmpty() && iterationCount++ < maxIterations) {
            Point current = queue.poll();

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                // Nur im erlaubten Bereich bleiben
                if (nx < minX || nx > maxX || ny < minY || ny > maxY) continue;

                Point neighbor = new Point(nx, ny);
                BlockType block = world.getBlock(nx, ny);
                if (!visited.contains(neighbor) && !block.isSolid()) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

    private static boolean hasAdjacentVisibleAir(Point cell, Set<Point> visibleAir) {
        for (int i = 0; i < DX.length; i++) {
            Point np = new Point(cell.x + DX[i], cell.y + DY[i]);
            if (visibleAir.contains(np)) return true;
        }
        return false;
    }

    private static BlockType getBlockAt(World world, int gx, int gy) {
        int chunkX = Math.floorDiv(gx, Chunk.WIDTH);
        Chunk chunk = world.getChunks().get(chunkX);
        if (chunk == null || gy < 0 || gy >= Chunk.HEIGHT) return BlockType.DIRT; // treat as solid to block flood
        int lx = Math.floorMod(gx, Chunk.WIDTH);
        return chunk.blocks[lx][gy];
    }

}
