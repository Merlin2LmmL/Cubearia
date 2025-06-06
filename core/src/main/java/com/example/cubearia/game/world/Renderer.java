package com.example.cubearia.game.world;

import com.badlogic.gdx.Gdx;
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

public class Renderer {
    private static final int[] DX = {1, -1, 0, 0};
    private static final int[] DY = {0, 0, 1, -1};
    private static int renderRadius;

    public void renderWorld(World world, Player player, SpriteBatch batch, OrthographicCamera camera) {
        renderRadius = Settings.RENDERDISTANCE;

        // 1) Sichtbare Luftblöcke ermitteln
        IntSet visibleAir = Settings.RENDERALLBLOCKS
            ? null
            : computeVisibleAir(world, player.getX(), player.getY());

        // 2) Kamera-Bounds in Tile-Koordinaten
        float camX = camera.position.x, camY = camera.position.y;
        float viewW = camera.viewportWidth  * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        int minXtile = (int)Math.floor((camX - viewW/2f) / TILE_SIZE);
        int maxXtile = (int)Math.floor((camX + viewW/2f) / TILE_SIZE);
        int minYtile = Math.max(0, (int)Math.floor((camY - viewH/2f) / TILE_SIZE));
        int maxYtile = Math.min(Chunk.HEIGHT - 1, (int)Math.floor((camY + viewH/2f) / TILE_SIZE));

        // 3) Horizontale Chunk-Range
        int playerBlockX = (int)Math.floor(player.getX());
        int chunkCenterX = Math.floorDiv(playerBlockX, Chunk.WIDTH);
        int cMin = chunkCenterX - renderRadius;
        int cMax = chunkCenterX + renderRadius;

        // Debug-Zähler
        int chunksProcessed = 0;
        int blocksRendered = 0;

        // 4) Render‐Loop
        for (IntMap.Entry<Chunk> entry : world.getChunks().entries()) {
            int cx = entry.key;
            if (cx < cMin || cx > cMax) continue;
            chunksProcessed++;

            Chunk chunk = entry.value;
            int baseX = cx * Chunk.WIDTH;

            int startLx = Math.max(0, minXtile - baseX - 1);
            int endLx   = Math.min(Chunk.WIDTH - 1, maxXtile - baseX);
            if (startLx > endLx) continue;

            for (int lx = startLx; lx <= endLx; lx++) {
                int gx = baseX + lx;
                for (int y = minYtile; y <= maxYtile; y++) {
                    int key = (gx << 16) | (y & 0xFFFF);
                    BlockType type = chunk.blocks[lx][y][1];
                    boolean isAir = type == BlockType.AIR;

                    boolean visible = Settings.RENDERALLBLOCKS
                        || (visibleAir != null && visibleAir.contains(key))
                        || (!isAir && hasAdjacent(visibleAir, key));

                    if (isAir && visibleAir != null && visibleAir.contains(key)) continue;

                    float dstX = gx * TILE_SIZE, dstY = y * TILE_SIZE;
                    if (!isAir && visible) {
                        TextureRegion region = type.getRegion();
                        if (region != null) {
                            batch.draw(region, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            blocksRendered++;
                        }
                    } else if (!Settings.RENDERALLBLOCKS) {
                        batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                        blocksRendered++;
                    }
                }
            }
        }

        // 5) Debug-Grid und Zähler ausgeben
        if (Settings.DEBUG) {
            batch.setColor(Color.GREEN);
            float heightPx = Chunk.HEIGHT * TILE_SIZE;
            for (int c = cMin; c <= cMax + 1; c++) {
                float x = c * Chunk.WIDTH * TILE_SIZE;
                batch.draw(Assets.whitePixel, x, 0, 1, heightPx);
            }
            batch.setColor(Color.WHITE);
            Gdx.app.log("RendererDebug", "Chunks: " + chunksProcessed + ", Blocks: " + blocksRendered);
        }
    }

    private static IntSet computeVisibleAir(World world, float px, float py) {
        IntSet vis = new IntSet();
        IntArray queue = new IntArray();

        int startX = (int)Math.floor(px);
        int startY = (int)Math.floor(py);

        int chunkX = Math.floorDiv(startX, Chunk.WIDTH);
        int chunkY = Math.floorDiv(startY, Chunk.HEIGHT);

        int minChunkX = chunkX - renderRadius;
        int maxChunkX = chunkX + renderRadius;
        int minChunkY = chunkY - renderRadius;
        int maxChunkY = chunkY + renderRadius;

        int minX = minChunkX * Chunk.WIDTH;
        int maxX = (maxChunkX + 1) * Chunk.WIDTH - 1;
        int minY = minChunkY * Chunk.HEIGHT;
        int maxY = (maxChunkY + 1) * Chunk.HEIGHT - 1;

        int startKey = (startX << 16) | (startY & 0xFFFF);
        vis.add(startKey);
        queue.add(startKey);

        int idx = 0, maxSteps = 5000 * Settings.RENDERDISTANCE;
        while (idx < queue.size && idx < maxSteps) {
            int key = queue.get(idx++);
            int x = key >> 16;
            int y = key & 0xFFFF;

            for (int d = 0; d < 4; d++) {
                int nx = x + DX[d], ny = y + DY[d];
                if (nx < minX || nx > maxX || ny < minY || ny > maxY) continue;

                int nk = (nx << 16) | (ny & 0xFFFF);
                if (vis.contains(nk)) continue;

                BlockType bt = getBlockSafe(world, nx, ny);
                if (bt != null && bt.isTransparent()) {
                    vis.add(nk);
                    queue.add(nk);
                }
            }
        }
        return vis;
    }

    private static boolean hasAdjacent(IntSet vis, int key) {
        if (vis == null) return false;
        int x = key >> 16, y = key & 0xFFFF;
        for (int i = 0; i < 4; i++) {
            int nk = ((x + DX[i]) << 16) | ((y + DY[i]) & 0xFFFF);
            if (vis.contains(nk)) return true;
        }
        return false;
    }

    private static BlockType getBlockSafe(World w, int gx, int gy) {
        int cx = Math.floorDiv(gx, Chunk.WIDTH);
        Chunk c = w.getChunks().get(cx);
        if (c == null || gy < 0 || gy >= Chunk.HEIGHT) return null;
        int localX = Math.floorMod(gx, Chunk.WIDTH);
        return c.blocks[localX][gy][1];
    }
}
