package com.example.cubearia.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.inventory.*;
import com.example.cubearia.game.inventory.crafting.DefaultRecipes;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.Renderer;
import com.example.cubearia.game.world.World;
import com.example.cubearia.ui.*;
import com.example.cubearia.ui.UIManager;

import javax.swing.*;

public class GameScreen implements Screen {
    public static final int TILE_SIZE = 16;

    private SpriteBatch batch;
    public OrthographicCamera camera;
    public OrthographicCamera uiCamera;
    public BitmapFont debugFont;
    private DebugRenderer debugRenderer;

    private World world;
    private Player player;
    private Inventory inventory;
    private UIManager uiManager;

    private final float DEADZONE_WIDTH  = 3 * TILE_SIZE;
    private final float DEADZONE_HEIGHT = 2 * TILE_SIZE;
    private final float CAMERA_LERP     = 8;
    private final float MAX_DISTANCE    = 8 * TILE_SIZE;
    private final float CAMERA_OFFSET_Y = -1 * TILE_SIZE;

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

        ItemRegistry.init();

        world = new World();
        inventory = new Inventory(9, 4);
        player    = new Player(world, camera, inventory);
        world.addEntity(player);
        uiManager = new UIManager(player, uiCamera, world, inventory);
    }

    private void update(float delta) {
        world.update(delta);
        float targetX = player.x * TILE_SIZE;
        float targetY = player.y * TILE_SIZE;

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

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom = MathUtils.clamp(camera.zoom + amountY * 0.1f, 0.2f, 3f);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            uiManager.toggleInventory();
        }

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiCamera.unproject(mouse);

        camera.update();

        if (!player.inMenu) {
            handleBlockBreaking();
            handleBlockPlacing();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            debugRenderer.toggle();
        }
    }

    public int getBlockGlobalXAtCursor() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        return toTileIndex(mousePos.x);
    }

    public int getBlockGlobalYAtCursor() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
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
        BlockType block = getBlockAtCursor();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (player.isInRange(globalX, globalY) && block.isBreakable()) {
                if (!isBreaking || globalX != breakingX || globalY != breakingY) {
                    breakingX = globalX;
                    breakingY = globalY;
                    breakingTimeTotal = (float)(block.getHardness() * 0.4f);
                    breakingTimer = 0f;
                    isBreaking = true;
                } else {
                    breakingTimer += Gdx.graphics.getDeltaTime();
                    if (breakingTimer >= breakingTimeTotal) {
                        world.setBlock(breakingX, breakingY, BlockType.AIR);
                        DroppedItem drop = new DroppedItem(world,
                            breakingX, breakingY + 0.25f,
                            Item.valueOf(block.name()),
                            MathUtils.random(5f, 8f) * (MathUtils.randomBoolean() ? 1 : -1),
                            MathUtils.random(2f, 3f),
                            1
                        );
                        world.addEntity(drop);
                        isBreaking = false;
                    }
                }
            } else {
                isBreaking = false;
            }
        } else {
            isBreaking = false;
        }
    }

    private void handleBlockPlacing() {
        if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) return;

        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();

        if (!player.isInRange(globalX, globalY)) return;
        if (world.getBlock(globalX, globalY) != BlockType.AIR && world.getBlock(globalX, globalY) != BlockType.WATER) return;
        if (player.intersects(globalX, globalY)) return;

        int slotIndex = player.selectedSlot;
        ItemStack stack = inventory.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return;

        BlockType blockToPlace = BlockType.valueOf(stack.getItem().getId().toUpperCase());
        world.setBlock(globalX, globalY, blockToPlace);

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

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

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

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        uiManager.render(batch);
        debugRenderer.render(batch, player, getBlockAtCursor(), world);
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
    @Override public void dispose() {
        batch.dispose();
        debugFont.dispose();
        Assets.dispose();
        debugRenderer.dispose();
    }
}
