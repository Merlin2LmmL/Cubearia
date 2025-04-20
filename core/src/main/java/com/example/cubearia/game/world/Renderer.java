package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
            visibleAir = computeVisibleAir(world, player.x);
        }

        // 2. Alle geladenen Chunks rendern
        for (Map.Entry<Integer, Chunk> entry : world.chunks.entrySet()) {
            int chunkX = entry.getKey();
            Chunk chunk = entry.getValue();

            if (chunkX >= (player.x / 16 - 1) - Settings.RENDERDISTANCE && chunkX <= (player.x / 16) + Settings.RENDERDISTANCE) {
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
                            int tx = type.getTileX();
                            int ty = type.getTileY();
                            batch.draw(Assets.blockTexture,
                                dstX, dstY, TILE_SIZE, TILE_SIZE,
                                tx * TILE_SIZE, ty * TILE_SIZE,
                                TILE_SIZE, TILE_SIZE,
                                false, false);
                        } else if (!Settings.RENDERALLBLOCKS) {
                                batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }
    }


    private static Set<Point> computeVisibleAir(World world, float playerX) {
        Set<Point> visited = new HashSet<>();
        Deque<Point> queue = new ArrayDeque<>();
        int maxIterations = 100000; // failsafe für infinite loops
        int iterationCount = 0;

        for (Map.Entry<Integer, Chunk> entry : world.chunks.entrySet()) {
            int chunkX = entry.getKey();
            if (chunkX >= (playerX / 16 - 1) - Settings.RENDERDISTANCE && chunkX <= (playerX / 16) + Settings.RENDERDISTANCE) {
                Chunk chunk = entry.getValue();

                for (int lx = 0; lx < Chunk.WIDTH; lx++) {
                    int gx = chunkX * Chunk.WIDTH + lx;
                    int gy = Chunk.HEIGHT - 1;

                    if (chunk.blocks[lx][gy] == BlockType.AIR) {
                        Point p = new Point(gx, gy);
                        visited.add(p);
                        queue.add(p);
                    }
                }
            }
        }

        while (!queue.isEmpty() && iterationCount++ < maxIterations) {
            Point p = queue.removeFirst();

            for (int i = 0; i < 4; i++) {
                int nx = p.x + DX[i];
                int ny = p.y + DY[i];
                Point np = new Point(nx, ny);
                if (visited.contains(np)) continue;
                if (getBlockAt(world, nx, ny) != BlockType.AIR) continue;

                visited.add(np);
                queue.addLast(np);
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
        Chunk chunk = world.chunks.get(chunkX);
        if (chunk == null || gy < 0 || gy >= Chunk.HEIGHT) return BlockType.DIRT; // treat as solid to block flood
        int lx = Math.floorMod(gx, Chunk.WIDTH);
        return chunk.blocks[lx][gy];
    }

}
