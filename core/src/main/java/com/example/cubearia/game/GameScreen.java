package com.example.cubearia.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.inventory.*;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.Renderer;
import com.example.cubearia.game.world.World;
import com.example.cubearia.ui.DebugRenderer;
import com.example.cubearia.ui.InventoryUI;

public class GameScreen implements Screen {
    public static final int TILE_SIZE = 16;

    private SpriteBatch batch;
    public OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private BitmapFont debugFont;
    private DebugRenderer debugRenderer;

    private World world;
    private Player player;
    private Inventory inventory;
    private InventoryUI inventoryUI;
    private Boolean debug = false;

    // in GameScreen:
    private final float DEADZONE_WIDTH  = 3 * TILE_SIZE;   // z.B. 3 Tiles links/rechts
    private final float DEADZONE_HEIGHT = 2 * TILE_SIZE;   // z.B. 2 Tiles oben/unten
    private final float CAMERA_LERP     = 8;              // je höher, desto schneller folgt die Kamera
    private final float MAX_DISTANCE    = 8 * TILE_SIZE;   // maximale Entfernung in Pixel
    private final float CAMERA_OFFSET_Y = -1 * TILE_SIZE;   // verschiebung

    private float breakingTimer = 0f;
    private float breakingTimeTotal = 0f;
    private int breakingX = -1, breakingY = -1;
    private boolean isBreaking = false;

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        debugRenderer = new DebugRenderer();

        debugFont = new BitmapFont();
        Assets.load();

        // Block Atlas initialisieren
        ItemRegistry.init(Assets.blockAtlas);

        world = new World();
        inventory = new Inventory(10);
        player    = new Player(world, camera, inventory);
        world.addEntity(player);
        inventoryUI = new InventoryUI(player.getInventory(), Assets.slotBackground, debugFont);
    }


    private void update(float delta) {
        world.update(delta);
        // Spieler‑Position in Welt‑Koordinaten
        float targetX = player.x * TILE_SIZE;
        float targetY = player.y * TILE_SIZE;

        // Aktuelle Kamera‑Pos
        Vector3 camPos = camera.position;
        float dx = targetX - camPos.x;
        float dy = targetY - camPos.y + CAMERA_OFFSET_Y * camera.zoom;

        // 1) Deadzone check: nur verschieben, wenn außerhalb der Box
        float moveX = 0, moveY = 0;
        if (Math.abs(dx) > DEADZONE_WIDTH * camera.zoom) {
            float excessX = Math.abs(dx) - DEADZONE_WIDTH * camera.zoom;
            moveX = Math.signum(dx) * excessX;
        }
        if (Math.abs(dy) > DEADZONE_HEIGHT * camera.zoom) {
            float excessY = Math.abs(dy) - DEADZONE_HEIGHT * camera.zoom;
            moveY = Math.signum(dy) * excessY;
        }

        // 2) Smoothing (Lerp): Kamera nähert sich dem Ziel schrittweise
        camPos.x += moveX * CAMERA_LERP * delta;
        camPos.y += moveY * CAMERA_LERP * delta;

        // 3) Max‑Distance Clamp: nie weiter als MAX_DISTANCE vom Spieler
        float dist = Vector2.dst(camPos.x, camPos.y, targetX, targetY);
        if (dist > MAX_DISTANCE) {
            // Verschiebe Kamera auf den Rand der erlaubten Kugel
            float angle = (float)Math.atan2(camPos.y - targetY, camPos.x - targetX);
            camPos.x = targetX + (float)Math.cos(angle) * MAX_DISTANCE;
            camPos.y = targetY + (float)Math.sin(angle) * MAX_DISTANCE;
        }

        // Scrool Faktor bekommen
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * 0.1f;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 3f);
                return true;
            }
        });


        camera.update();

        // Input-Handling für Blockabbau/platzierung
        handleBlockBreaking();
        handleBlockPlacing();

        // Debug Modus toggeln
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            debugRenderer.toggle();
        }
    }

    public int getBlockGlobalXAtCursor() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        int globalX = toTileIndex(mousePos.x);
        return toTileIndex(mousePos.x);
    }

    public int getBlockGlobalYAtCursor() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        int globalX = toTileIndex(mousePos.y);
        return toTileIndex(mousePos.y);
    }

    public BlockType getBlockAtCursor() {
        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();

        return world.getBlock(globalX, globalY);
    }

    private void handleBlockBreaking() {
        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();

        // Block auf der Maus
        BlockType block = getBlockAtCursor();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (player.isInRange(globalX, globalY) && block.isBreakable()) {
                // Wenn ein neuer Block angeklickt wird → Abbau starten
                if (!isBreaking || globalX != breakingX || globalY != breakingY) {
                    breakingX = globalX;
                    breakingY = globalY;
                    breakingTimeTotal = (float) (block.hardness * 0.4f);
                    breakingTimer = 0f;
                    isBreaking = true;
                } else {
                    // Weiter abbauen, wenn gleich geblieben
                    breakingTimer += Gdx.graphics.getDeltaTime();
                    if (breakingTimer >= breakingTimeTotal) {
                        world.setBlock(breakingX, breakingY, BlockType.AIR); Item dropped = Item.valueOf(block.name());
                        DroppedItem drop = new DroppedItem(world,
                            breakingX, breakingY + 0.25f,   // Drop-Position
                            Item.valueOf(block.name()),
                            MathUtils.random(5f, 8f) * (MathUtils.randomBoolean() ? 1 : -1), MathUtils.random(2f, 3f),
                            1
                        );
                        world.addEntity(drop);
                        isBreaking = false;
                    }
                }
            } else {
                // Maus außerhalb Reichweite oder Block nicht abbaubar
                isBreaking = false;
            }
        } else {
            // Maustaste losgelassen → Abbruch
            isBreaking = false;
        }
    }

    private void handleBlockPlacing() {
        // 1) Nur einmal pro Klick
        if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) return;

        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();

        // 2) Reichweite prüfen
        if (!player.isInRange(globalX, globalY)) return;

        // 3) Nur auf Luft setzen
        if (world.getBlock(globalX, globalY) != BlockType.AIR) return;

        if (player.intersects(globalX, globalY)) return;

        // 4) Aktuellen Slot auslesen
        int slotIndex = player.selectedSlot;
        ItemStack stack = inventory.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return;

        // 5) BlockType per Name mappen
        BlockType blockToPlace;
        blockToPlace = BlockType.valueOf(stack.getItem().getId().toUpperCase());
        // 6) Block setzen
        world.setBlock(globalX, globalY, blockToPlace);

        // 7) Inventar updaten
        stack.decrease(1);
        if (stack.isEmpty()) {
            inventory.removeItem(slotIndex);
        }
    }

    private int toTileIndex(float pixelCoord) {
        return (int)Math.floor(pixelCoord / TILE_SIZE);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.3f, 0.6f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1) Welt zeichnen
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Gegeben: Das Tilesheet ist in 4 Spalten angeordnet.
        final int COLUMNS = 4;

        Renderer.renderWorld(world, player, batch);

        if (isBreaking) {
            float progress = breakingTimer / breakingTimeTotal;
            int frameIndex = Math.min((int)(progress * 4), 3);
            TextureRegion currentFrame = Assets.breakingFrames[frameIndex];
            batch.draw(currentFrame, breakingX * TILE_SIZE, breakingY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        world.renderEntities(batch);

        batch.end();

        // 2) UI zeichnen (Inventar, Debug‑Overlay usw.)
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        inventoryUI.render(batch, uiCamera, player.selectedSlot);
        debugRenderer.render(batch, player, getBlockAtCursor(), world);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Welt‑Kamera
        int tilesVisibleY = 30;
        int tilesVisibleX = (int)(tilesVisibleY * (float)width / height);
        camera.setToOrtho(false, tilesVisibleX * TILE_SIZE, tilesVisibleY * TILE_SIZE);

        // UI‑Kamera
        uiCamera.setToOrtho(false, tilesVisibleX * TILE_SIZE, tilesVisibleY * TILE_SIZE);
    }


    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        batch.dispose();
        debugFont.dispose();
        Assets.dispose();
        debugRenderer.dispose();
    }
}
