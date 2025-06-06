package com.example.cubearia.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.entities.DroppedItem;
import com.example.cubearia.game.entities.Sheep;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemRegistry;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Renderer;
import com.example.cubearia.game.world.World;
import com.example.cubearia.ui.DebugRenderer;
import com.example.cubearia.ui.UIManager;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    public static final int TILE_SIZE = 16;

    private SpriteBatch batch;
    public OrthographicCamera camera;
    public OrthographicCamera uiCamera;
    public BitmapFont debugFont;
    private DebugRenderer debugRenderer;

    private World world;
    private Player localPlayer;
    private Sheep sheep;
    private Inventory inventory;
    private UIManager uiManager;

    private final float DEADZONE_WIDTH  = 3 * TILE_SIZE;
    private final float DEADZONE_HEIGHT = 2 * TILE_SIZE;
    private final float CAMERA_LERP     = 8;
    private final float MAX_DISTANCE    = 8 * TILE_SIZE;
    private final float CAMERA_OFFSET_Y = 2f * TILE_SIZE;
    private int max_zoom;

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

        ItemRegistry.init();

        world = new World();
        inventory = new Inventory(9, 4);
        localPlayer = new Player(world, camera, inventory);
        world.addEntity(localPlayer);
        sheep = new Sheep(world, camera);
        world.addEntity(sheep);
        uiManager = new UIManager(localPlayer, uiCamera, world, inventory);
        max_zoom = Settings.DEBUG ? 80 : 10;
    }

    private void update(float delta) {
        // 1) Welt aktualisieren (bewegt z.B. fallende DroppedItems, Spieler‐Bewegung, etc.)
        world.update(delta);

        // 2) Kamera nur auf den lokalen Spieler ausrichten
        float targetX = localPlayer.getX() * TILE_SIZE;
        float targetY = localPlayer.getY() * TILE_SIZE;
        Vector3 camPos = camera.position;
        float dx = targetX - camPos.x;
        float dy = targetY - camPos.y + CAMERA_OFFSET_Y * camera.zoom;

        float moveX = 0, moveY = 0;
        if (Math.abs(dx) > DEADZONE_WIDTH * camera.zoom) {
            float excessX = Math.abs(dx) - DEADZONE_WIDTH * camera.zoom;
            moveX = Math.signum(dx) * excessX;
        }
        if (Math.abs(dy) > DEADZONE_HEIGHT * camera.zoom) {
            float excessY = Math.abs(dy) - DEADZONE_HEIGHT * camera.zoom;
            moveY = Math.signum(dy) * excessY;
        }

        camPos.x += moveX * CAMERA_LERP * delta;
        camPos.y += moveY * CAMERA_LERP * delta;

        float dist = Vector2.dst(camPos.x, camPos.y, targetX, targetY);
        if (dist > MAX_DISTANCE) {
            float angle = (float)Math.atan2(camPos.y - targetY, camPos.x - targetX);
            camPos.x = targetX + (float)Math.cos(angle) * MAX_DISTANCE;
            camPos.y = targetY + (float)Math.sin(angle) * MAX_DISTANCE;
        }

        // 3) Input‐Processor für Zoom & UI
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom = MathUtils.clamp(
                    camera.zoom + amountY * camera.zoom * 0.5f,
                    0.1f, max_zoom
                );
                return true;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                uiManager.pressClick(screenX, screenY, button);
                return true;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                uiManager.releaseClick(screenX, screenY, button);
                return true;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                uiManager.updateMousePosition(screenX, screenY);
                return true;
            }
        });

        Settings.RENDERDISTANCE = (int) (camera.zoom * 0.9f + 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            uiManager.toggleInventory();
        }

        // Crafting-Table Interaktion (nur lokalPlayer)
        if (localPlayer.getBlockAtCursor() == BlockType.CRAFTING_TABLE
            && Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)
            && localPlayer.isInRange(localPlayer.getBlockGlobalXAtCursor(), localPlayer.getBlockGlobalYAtCursor())
            && !localPlayer.inMenu) {
            uiManager.toggleCraftingTable();
        }

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiCamera.unproject(mouse);

        camera.update();

        // 4) Alle Spieler‐Entitäten abarbeiten und ggf. Drops sammeln
        List<DroppedItem> toAdd = new ArrayList<>();
        for (com.example.cubearia.game.entities.Entity e : world.getEntities()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                if (!p.inMenu) {
                    DroppedItem drop = p.handleBlockBreaking();
                    if (drop != null) {
                        toAdd.add(drop);
                    }
                    p.handleBlockPlacing();
                }
            }
        }

        // 5) Erst NACHDEM wir alle Spieler durchlaufen haben, fügen wir neue Drops zur Welt hinzu
        for (DroppedItem d : toAdd) {
            world.addEntity(d);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            debugRenderer.toggle();
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0.3f, 0.6f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        new Renderer().renderWorld(world, localPlayer, batch, camera);

        for (com.example.cubearia.game.entities.Entity e : world.getEntities()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                if (p.isBreaking()) {
                    TextureRegion frame = p.getBreakingFrame();
                    int bx = p.getBreakingX();
                    int by = p.getBreakingY();
                    batch.draw(
                        frame,
                        bx * TILE_SIZE,
                        by * TILE_SIZE,
                        TILE_SIZE,
                        TILE_SIZE
                    );
                }
            }
        }

        world.renderEntities(batch);

        world.renderParticles(batch);

        batch.end();

        // 4) UI‐Schicht (Inventar, Crafting etc.) nur für localPlayer
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        uiManager.render(batch);
        debugRenderer.render(batch, localPlayer, localPlayer.getBlockAtCursor(), world);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        int tilesVisibleY = 30;
        int tilesVisibleX = (int)(tilesVisibleY * (float)width / height);
        camera.setToOrtho(false, tilesVisibleX * TILE_SIZE, tilesVisibleY * TILE_SIZE);
        uiCamera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        debugFont.dispose();
        Assets.dispose();
        debugRenderer.dispose();
    }
}
