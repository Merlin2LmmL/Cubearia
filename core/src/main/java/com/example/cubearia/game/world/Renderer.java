package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.Color;
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

    // Erweitertes Sichtfeld um einen Chunk nach hinten
    private static final int BACK_EXTRA = 1;
    private static final int RADIUS_IN_CHUNKS = Settings.RENDERDISTANCE + BACK_EXTRA;

    public static void renderWorld(World world, Player player, SpriteBatch batch) {
        Set<Point> visibleAir = new HashSet<>();
        if (!Settings.RENDERALLBLOCKS) {
            visibleAir = computeVisibleAir(world, player.x, player.y);
        }

        int playerChunk = Math.floorDiv(Math.round(player.x), Chunk.WIDTH);
        int minChunk = playerChunk - Settings.RENDERDISTANCE - BACK_EXTRA;
        int maxChunk = playerChunk + Settings.RENDERDISTANCE + BACK_EXTRA;

        for (Map.Entry<Integer, Chunk> entry : world.getChunks().entrySet()) {
            int chunkX = entry.getKey();
            Chunk chunk = entry.getValue();

            if (chunkX >= minChunk && chunkX <= maxChunk) {
                for (int lx = 0; lx < Chunk.WIDTH; lx++) {
                    for (int y = 0; y < Chunk.HEIGHT; y++) {
                        int gx = chunkX * Chunk.WIDTH + lx;
                        int gy = y;
                        Point p = new Point(gx, gy);
                        BlockType type = chunk.blocks[lx][y];
                        boolean isAir = type == BlockType.AIR;

                        boolean isVisible = Settings.RENDERALLBLOCKS || visibleAir.contains(p)
                            || (!isAir && hasAdjacentVisibleAir(p, visibleAir));

                        float dstX = gx * TILE_SIZE;
                        float dstY = gy * TILE_SIZE;

                        // Luftblöcke überspringen, die sichtbar sind
                        if (isAir && visibleAir.contains(p)) {
                            continue;
                        }

                        if (!isAir && isVisible) {
                            TextureRegion region = type.getRegion();
                            if (region != null) {
                                batch.draw(region, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            }
                        } else if (!Settings.RENDERALLBLOCKS) {
                            batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }

        if (Settings.DEBUG) {
            int minBoundary = playerChunk - Settings.RENDERDISTANCE + 1 - BACK_EXTRA;
            int maxBoundary = playerChunk + Settings.RENDERDISTANCE + 1 + BACK_EXTRA;
            float worldHeightPixels = Chunk.HEIGHT * TILE_SIZE;

            batch.setColor(Color.GREEN);
            for (int boundary = minBoundary; boundary <= maxBoundary; boundary++) {
                float lineX = boundary * Chunk.WIDTH * TILE_SIZE;
                batch.draw(Assets.whitePixel, lineX, 0, 1, worldHeightPixels);
            }
            batch.setColor(Color.WHITE);
        }
    }

    private static Set<Point> computeVisibleAir(World world, float playerX, float playerY) {
        Set<Point> visited = new HashSet<>();
        Deque<Point> queue = new ArrayDeque<>();
        int maxIterations = 100_000;
        int iterationCount = 0;

        int startX = (int) playerX;
        int startY = (int) playerY;

        int minX = ((startX / Chunk.WIDTH) - RADIUS_IN_CHUNKS) * Chunk.WIDTH;
        int maxX = ((startX / Chunk.WIDTH) + RADIUS_IN_CHUNKS + 1) * Chunk.WIDTH - 1;
        int minY = 0;
        int maxY = Chunk.HEIGHT - 1;

        Point start = new Point(startX, startY);
        BlockType startBlock = getBlockSafe(world, startX, startY);

        if (startBlock == null || startBlock.isSolid()) {
            boolean found = false;
            for (int i = 0; i < 4; i++) {
                int nx = startX + DX[i];
                int ny = startY + DY[i];
                BlockType neighbor = getBlockSafe(world, nx, ny);
                if (neighbor != null && !neighbor.isSolid()) {
                    startX = nx;
                    startY = ny;
                    start = new Point(startX, startY);
                    found = true;
                    break;
                }
            }
            if (!found) return visited;
        }

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty() && iterationCount++ < maxIterations) {
            Point current = queue.poll();

            for (int i = 0; i < 4; i++) {
                int nx = current.x + DX[i];
                int ny = current.y + DY[i];

                if (nx < minX || nx > maxX || ny < minY || ny > maxY) continue;

                Point neighbor = new Point(nx, ny);
                BlockType block = getBlockSafe(world, nx, ny);
                if (block != null && !block.isSolid() && !visited.contains(neighbor)) {
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

    private static BlockType getBlockSafe(World world, int gx, int gy) {
        int chunkX = Math.floorDiv(gx, Chunk.WIDTH);
        Chunk chunk = world.getChunks().get(chunkX);
        if (chunk == null || gy < 0 || gy >= Chunk.HEIGHT) return null;
        int lx = Math.floorMod(gx, Chunk.WIDTH);
        return chunk.blocks[lx][gy];
    }
}
