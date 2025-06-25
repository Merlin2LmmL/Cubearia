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

    /** Gespeichertes sichtbares-Air-Set für den aktuellen Frame (oder null bei RENDERALLBLOCKS). */
    private IntSet visibleAir;

    /** Muss zu Beginn jedes Frames aufgerufen werden, bevor renderWorld aufgerufen wird. */
    public void computeVisibility(World world, Player player) {
        renderRadius = Settings.RENDERDISTANCE;
        if (Settings.RENDERALLBLOCKS) {
            visibleAir = null;
        } else {
            if (Settings.USE_RAYCASTING_VISIBILITY) {
                visibleAir = computeVisibleAirWithRaycasting(world, player.getX(), player.getY());
            } else {
                visibleAir = computeVisibleAir(world, player.getX(), player.getY());
            }
        }
    }

    /**
     * Rendert die Welt mit Layer-System.
     * Verwendet das zuvor in computeVisibility gesetzte visibleAir.
     */
    public void renderWorld(World world, Player player, SpriteBatch batch, OrthographicCamera camera) {
        if (visibleAir == null && !Settings.RENDERALLBLOCKS) {
            // Falls computeVisibility nicht aufgerufen wurde, abbrechen
            Gdx.app.log("Renderer", "visibleAir nicht berechnet, skipping renderWorld");
            return;
        }
        renderRadius = Settings.RENDERDISTANCE;

        // 1) Kamera-Bounds in Tile-Koordinaten
        float camX = camera.position.x, camY = camera.position.y;
        float viewW = camera.viewportWidth  * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        int minXtile = (int)Math.floor((camX - viewW/2f) / TILE_SIZE);
        int maxXtile = (int)Math.floor((camX + viewW/2f) / TILE_SIZE);
        int minYtile = Math.max(0, (int)Math.floor((camY - viewH/2f) / TILE_SIZE));
        int maxYtile = Math.min(Chunk.HEIGHT - 1, (int)Math.floor((camY + viewH/2f) / TILE_SIZE));

        // 2) Horizontale Chunk-Range
        int playerBlockX = (int)Math.floor(player.getX());
        int chunkCenterX = Math.floorDiv(playerBlockX, Chunk.WIDTH);
        int cMin = chunkCenterX - renderRadius;
        int cMax = chunkCenterX + renderRadius;

        // Debug-Zähler
        int chunksProcessed = 0;
        int blocksRendered = 0;

        // 3) Render‐Loop über Chunks/Positionen
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

                    boolean posVisible;
                    if (visibleAir == null) {
                        // RENDERALLBLOCKS: alles sichtbar
                        posVisible = true;
                    } else {
                        if (visibleAir.contains(key)) {
                            posVisible = true;
                        } else {
                            posVisible = hasAdjacent(visibleAir, key);
                        }
                    }

                    float dstX = gx * TILE_SIZE, dstY = y * TILE_SIZE;

                    // Hole beide Layer-Blöcke
                    BlockType frontBlock = world.getBlock(gx, y, 0);
                    BlockType backBlock = world.getBlock(gx, y, 1);

                    if (frontBlock.isTransparent() || frontBlock == BlockType.AIR) {
                        TextureRegion backRegion = backBlock.getRegion();
                        if (backRegion != null) {
                            // Setze Farbe basierend auf Sichtbarkeit und Blocktyp
                            if (!posVisible) {
                                // Verwende schwarze Textur für unsichtbare Blöcke
                                batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            } else if (backBlock != BlockType.WATER) {
                                // Erze im Layer 1 haben 15% mehr Tint
                                if (backBlock == BlockType.COAL_ORE ||
                                    backBlock == BlockType.IRON_ORE ||
                                    backBlock == BlockType.GOLD_ORE ||
                                    backBlock == BlockType.DIAMOND_ORE) {
                                    batch.setColor(0.7f, 0.7f, 0.7f, 1f); // 15% dunkler als 0.85f
                                } else {
                                    batch.setColor(0.85f, 0.85f, 0.85f, 1f);
                                }
                                batch.draw(backRegion, dstX, dstY, TILE_SIZE, TILE_SIZE);
                                batch.setColor(Color.WHITE); // Farbe zurücksetzen
                            } else {
                                // Wasser normal rendern
                                batch.draw(backRegion, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            }
                            blocksRendered++;
                        }
                    }

                    // Rendere Vorder-Layer (wenn vorhanden)
                    if (frontBlock != BlockType.AIR) {
                        TextureRegion frontRegion = frontBlock.getRegion();
                        if (frontRegion != null) {
                            // Setze Farbe basierend auf Sichtbarkeit
                            if (!posVisible) {
                                // Verwende schwarze Textur für unsichtbare Blöcke
                                batch.draw(Assets.blackPixel, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            } else {
                                batch.draw(frontRegion, dstX, dstY, TILE_SIZE, TILE_SIZE);
                            }
                            blocksRendered++;
                        }
                    }
                }
            }
        }

        // 4) Debug-Grid und Zähler ausgeben
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

    /**
     * Führt Raycasting vom Spieler zu allen Kanten des Renderbereichs durch,
     * um eine lückenlose Sichtbarkeitskarte zu erstellen.
     */
    private static IntSet computeVisibleAirWithRaycasting(World world, float px, float py) {
        IntSet vis = new IntSet();
        int startX = (int)Math.floor(px);
        int startY = (int)Math.floor(py);

        vis.add((startX << 16) | (startY & 0xFFFF));

        int radius = renderRadius * Chunk.WIDTH;
        if (radius <= 0) return vis;

        // Wir casten Strahlen zu jedem Block am Rand eines Quadrats um den Spieler.
        // Das ist robuster als das Casten nach Winkeln, was Lücken hinterlassen kann.
        int p_min = -radius;
        int p_max = radius;

        for (int x = p_min; x <= p_max; x++) {
            castRay(world, startX, startY, startX + x, startY + p_max, vis); // Oberer Rand
            castRay(world, startX, startY, startX + x, startY + p_min, vis); // Unterer Rand
        }
        for (int y = p_min + 1; y < p_max; y++) {
            castRay(world, startX, startY, startX + p_max, startY + y, vis); // Rechter Rand
            castRay(world, startX, startY, startX + p_min, startY + y, vis); // Linker Rand
        }

        return vis;
    }

    /**
     * Castet einen einzelnen Strahl mit dem Bresenham-Algorithmus.
     * Stoppt, wenn ein undurchsichtiger Block getroffen wird.
     */
    private static void castRay(World world, int x0, int y0, int x1, int y1, IntSet vis) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            int key = (x0 << 16) | (y0 & 0xFFFF);
            vis.add(key);

            BlockType block = getBlockSafe(world, x0, y0);

            // Stoppe den Strahl, NACHDEM der undurchsichtige Block hinzugefügt wurde, damit die Wand sichtbar ist.
            if (block != null && !block.isTransparent()) {
                break;
            }

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /** Alte Flood-Fill-Methode (jetzt unbenutzt, kann als Fallback dienen). */
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

    /** Für Sichtbarkeit prüfen - nur Layer 0 verwenden */
    private static BlockType getBlockSafe(World w, int gx, int gy) {
        int cx = Math.floorDiv(gx, Chunk.WIDTH);
        Chunk c = w.getChunks().get(cx);
        if (c == null || gy < 0 || gy >= Chunk.HEIGHT) return null;
        return w.getBlock(gx, gy, 0);
    }
}
