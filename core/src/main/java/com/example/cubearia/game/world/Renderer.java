package com.example.cubearia.game.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntArray;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.entities.Player;

import static com.example.cubearia.game.GameScreen.TILE_SIZE;

/**
 * Renderer mit weiter optimiertem Culling und ohne Objekterzeugung pro Block.
 */
public class Renderer {
    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};
    private static int renderRadius;

    public void renderWorld(World world, Player player, SpriteBatch batch, OrthographicCamera camera) {
        renderRadius = Settings.RENDERDISTANCE;

        // Sichtbare Luftpunkte (primitive Keys)
        IntSet visibleAir = Settings.RENDERALLBLOCKS ? null : computeVisibleAir(world, player.x, player.y);

        // Kamera-Bounds in Tile-Koordinaten
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        int minXtile = (int)((camX - viewW/2f) / TILE_SIZE);
        int maxXtile = (int)((camX + viewW/2f) / TILE_SIZE);
        int minYtile = Math.max(0, (int)((camY - viewH/2f) / TILE_SIZE));
        int maxYtile = Math.min(Chunk.HEIGHT - 1, (int)((camY + viewH/2f) / TILE_SIZE));

        // Chunk-Bereich horizontal
        int cCenter = Math.floorDiv(Math.round(player.x), Chunk.WIDTH);
        int cMin = cCenter - Settings.RENDERDISTANCE;
        int cMax = cCenter + Settings.RENDERDISTANCE;

        // Renderschleife mit beidseitigem Culling
        for (IntMap.Entry<Chunk> entry : world.getChunks().entries()) {
            int cx = entry.key;
            if (cx < cMin || cx > cMax) continue;
            Chunk chunk = entry.value;
            int baseX = cx * Chunk.WIDTH;

            // Lokaler X-Bereich pro Chunk
            int startLx = Math.max(0, minXtile - baseX - 1); // zusätzlich eine Spalte links rendern
            int endLx   = Math.min(Chunk.WIDTH - 1, maxXtile - baseX);
            if (startLx > endLx) continue;

            for (int lx = startLx; lx <= endLx; lx++) {
                int gx = baseX + lx;
                for (int y = minYtile; y <= maxYtile; y++) {
                    int key = (gx << 16) | y;
                    BlockType type = chunk.blocks[lx][y][1];
                    boolean air = (type == BlockType.AIR);
                    boolean vis = Settings.RENDERALLBLOCKS
                        || (visibleAir != null && visibleAir.contains(key))
                        || (!air && hasAdjacent(visibleAir, key));

                    if (air && visibleAir != null && visibleAir.contains(key)) continue;
                    float dstX = gx * TILE_SIZE;
                    float dstY = y * TILE_SIZE;
                    if (!air && vis) {
                        TextureRegion r = type.getRegion();
                        if (r != null) batch.draw(r, dstX, dstY, TILE_SIZE, TILE_SIZE);
                    } else if (!Settings.RENDERALLBLOCKS) {
                        batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        // Debug-Grid (unverändert)
        if (Settings.DEBUG) {
            batch.setColor(Color.GREEN);
            float worldHpix = Chunk.HEIGHT * TILE_SIZE;
            int minBoundary = cMin + 1;
            int maxBoundary = cMax + 1;
            for (int b = minBoundary; b <= maxBoundary; b++) {
                float x = b * Chunk.WIDTH * TILE_SIZE;
                batch.draw(Assets.whitePixel, x, 0, 1, worldHpix);
            }
            batch.setColor(Color.WHITE);
        }
    }

    // computeVisibleAir unverändert
    private static IntSet computeVisibleAir(World world, float px, float py) {
        IntSet vis = new IntSet();
        IntArray queue = new IntArray();
        int startX = (int) px, startY = (int) py;
        int minX = ((startX / Chunk.WIDTH) - renderRadius) * Chunk.WIDTH;
        int maxX = ((startX / Chunk.WIDTH) + renderRadius + 1) * Chunk.WIDTH - 1;
        int startKey = (startX << 16) | startY;
        vis.add(startKey);
        queue.add(startKey);
        int idx = 0;
        while (idx < queue.size && idx < 100_000) {
            int k = queue.get(idx++);
            int x = k >> 16, y = k & 0xFFFF; // use arithmetic shift for negative coords
            for (int i = 0; i < 4; i++) {
                int nx = x + DX[i], ny = y + DY[i];
                if (nx < minX || nx > maxX || ny < 0 || ny >= Chunk.HEIGHT) continue;
                int nk = (nx << 16) | ny;
                if (vis.contains(nk)) continue;
                BlockType b = getBlockSafe(world, nx, ny);
                if (b != null && b.isTransparent()) {
                    vis.add(nk);
                    queue.add(nk);
                }
            }
        }
        return vis;
    }

    private static boolean hasAdjacent(IntSet vis, int key) {
        if (vis == null) return false;
        int x = key >> 16, y = key & 0xFFFF; // arithmetic shift for negatives
        for (int i = 0; i < 4; i++) {
            int nk = ((x + DX[i]) << 16) | (y + DY[i]);
            if (vis.contains(nk)) return true;
        }
        return false;
    }

    private static BlockType getBlockSafe(World w, int gx, int gy) {
        int cx = Math.floorDiv(gx, Chunk.WIDTH);
        Chunk c = w.getChunks().get(cx);
        if (c == null || gy < 0 || gy >= Chunk.HEIGHT) return null;
        return c.blocks[Math.floorMod(gx, Chunk.WIDTH)][gy][1];
    }
}
