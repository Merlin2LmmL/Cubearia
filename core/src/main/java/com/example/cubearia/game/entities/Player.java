package com.example.cubearia.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.GameScreen;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.World;

public class Player extends Entity {
    private final OrthographicCamera camera;
    private final Inventory inventory;

    public float velocityX = 0f;
    public float velocityY = 0f;

    public final float gravity = -40f;
    public final float jumpVelocity = (float) (10f * Math.sqrt(size));
    public final float moveAcceleration = 30f * size;
    public final float airControlFactor = 0.4f;
    public final float friction = 100f;
    public float finalmaxSpeed = 6f * size;
    public final int range = 5 * size;

    public int selectedSlot = 0;
    public boolean inventoryOpen;
    public boolean inMenu;

    public boolean onGround = false;
    private boolean inFluid = false;

    private float stateTime = 0f;
    private float walkCycleTime = 0f;
    private boolean flip = false;

    private float norminatedHight = 0;
    private float temperatureDamage = 0;

    private final TextureRegion headRegion;
    private final TextureRegion bodyRegion;
    private final TextureRegion leftlegRegion;
    private final TextureRegion rightlegRegion;
    private final TextureRegion leftarmRegion;
    private final TextureRegion rightarmRegion;

    private static int size = 2;

    private static final float LEG_AMPLITUDE = 20f;       // Maximum rotation angle
    private static final float LEG_FREQUENCY = 10f / size;       // Speed of cycle
    private static final float LEG_CHUNK_STEP = 7f;      // Chunky rotation step

    private final World world;

    public Player(World world, OrthographicCamera camera, Inventory inventory) {
        super(0, Chunk.HEIGHT, 0.375f * size, 1.82f * size);
        this.world = world;
        this.camera = camera;
        this.inventory = inventory;
        inventory.addItem(Item.valueOf("DIRT"), 64 * 4);
        inventory.addItem(Item.valueOf("OAK_LOG"), 64 * 4);
        inventory.addItem(Item.valueOf("COBBLESTONE"), 64 * 4);
        // Prepare texture regions
        headRegion = new TextureRegion(Assets.playerTexture,        6 * size,   0 * size,   8 * size, 8 * size);
        bodyRegion = new TextureRegion(Assets.playerTexture,        0 * size,   0 * size,   6 * size, 12 * size);
        leftlegRegion  = new TextureRegion(Assets.playerTexture,    0 * size,   12 * size,  3 * size, 10 * size);
        rightlegRegion  = new TextureRegion(Assets.playerTexture,   3 * size,   12 * size,  3 * size, 10 * size);
        leftarmRegion  = new TextureRegion(Assets.playerTexture,    6 * size,   8 * size,   4 * size, 11 * size);
        rightarmRegion  = new TextureRegion(Assets.playerTexture,   10 * size,  8 * size,   4 * size, 11 * size);
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            y = Chunk.HEIGHT;
            x = (float) (Math.random() - 0.5) * 20000;
        }

        stateTime += delta;

        boolean left  = false;
        boolean right = false;
        if (!inMenu) {
            left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            for (int i = 0; i <= 9; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                    selectedSlot = i - 1;
                }
            }
        }

        checkFluidState();

        // Walk Cycle
        if (velocityX != 0) {
            walkCycleTime += Math.abs(velocityX) * delta * (onGround ? 0.3f : 0.1f);
        } else {
            walkCycleTime = 0f;
        }

        float accel = moveAcceleration * delta ;
        float airAccel = accel * airControlFactor;

        if (onGround) {
            if (left) velocityX -= accel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += accel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta);
        } else {
            if (left) velocityX -= airAccel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += airAccel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta / 10);
        }

        float maxSpeed = onGround ? finalmaxSpeed : inFluid ? finalmaxSpeed * 0.5f : finalmaxSpeed * 1.2f;
        maxSpeed = ((facingLeft() ? true : false) == (left ? true : false)) ? maxSpeed : maxSpeed * 0.5f;
        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.SPACE))) {
            if (onGround) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inFluid) {
                velocityY += 0.5f;
            }
        }

        if (inFluid) {
            velocityY += gravity * 0.2f * delta; // weniger Gravitation im Wasser
            if (world.getBlock((int) x, (int) (Math.round(y + 0.25f))) == BlockType.WATER) {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f); // nicht zu schnell fallen
            } else {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f); // nicht zu schnell fallen
            }
        } else {
            velocityY += gravity * delta;
        }

        // Movement + Kollision
        moveWithCollision(delta);

        // Umgebung ggf. neu laden
        world.updateAroundPlayer(x);

        norminatedHight = 0.3f * (float) Math.pow((double) (world.getBiomeTypeAt((int) y).getTemperatureValue()), 2) - ((y - Settings.SEA_LEVEL) / (Settings.SEA_LEVEL));
        norminatedHight = MathUtils.clamp(norminatedHight, 0, 1);
        temperatureDamage = (2/(1-norminatedHight) - 2);
    }

    // Hilfsmethode zur Bodenreibung
    private void applyFriction(float frictionAmount) {
        if (velocityX > 0) {
            velocityX -= friction * frictionAmount;
            if (velocityX < 0) velocityX = 0;
        } else if (velocityX < 0) {
            velocityX += friction * frictionAmount;
            if (velocityX > 0) velocityX = 0;
        }
    }

    // Bewegung und Block-Kollisionen
    private void moveWithCollision(float delta) {
        // Merke alten Zustand
        float oldX = x, oldY = y;

        // 1) X‑Bewegung + Kollision
        x += velocityX * delta;
        if (checkCollision()) {
            // Bei Kollision: zurücksetzen
            x = oldX;
            velocityX = 0;
        }

        // 2) Y‑Bewegung + Kollision
        y += velocityY * delta;
        if (checkCollision()) {
            // Bei Kollision:
            y = oldY;
            if (velocityY < 0) onGround = true;
            velocityY = 0;
        } else {
            onGround = false;
        }
    }


    private boolean checkCollision() {
        int startX = (int)Math.floor(x);
        int endX   = (int)Math.ceil(x + width);
        int startY = (int)Math.floor(y);
        int endY   = (int)Math.ceil(y + height);
        for (int tx = startX; tx < endX; tx++) {
            for (int ty = startY; ty < endY; ty++) {
                if (world.getBlock(tx, ty).isSolid()) return true;
            }
        }
        return false;
    }

    private void checkFluidState() {
        inFluid = world.getBlock((int) x, (int) y).isFluid();
    }

    public boolean isInRange(int blockX, int blockY) {
        float dx = blockX - x;
        float dy = blockY - y;
        return dx*dx + dy*dy <= range*range;
    }

    public boolean intersects(int globalX, int globalY) {
        return (globalX == (int) x && globalY == (int) y) || (globalX == (int) x && globalY == (int) y + 1);
    }

    public void render(SpriteBatch batch) {
        renderAt(batch, x, y, size);
    }

    public void renderAt(SpriteBatch batch, float x, float y, int size) {
        float scale = (GameScreen.TILE_SIZE / 16f) * size;
        float baseX = x * GameScreen.TILE_SIZE;
        float baseY = y * GameScreen.TILE_SIZE;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        float headSize = 8 * scale;
        float headOffsetX = flip
            ? bodyRegion.getRegionWidth() * scale - headSize - 4 * scale
            : -1 * scale;
        float headOffsetY = 22 * scale;

        float headX = baseX + headOffsetX;
        float headY = baseY + headOffsetY;

        float originX = headSize / 2f;
        float originY = 0f;
        float pivotX = headX + originX;
        float pivotY = headY + originY;

        float angleRad = (float)Math.atan2(
            mouse.y - pivotY - 4 * scale, // vorher: 4 * size → falsch
            mouse.x - pivotX
        );
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        boolean headFlip = mouse.x < pivotX;
        float dir = headFlip ? -1f : 1f;

        float finalAngle = headFlip ? 180f + angleDeg : angleDeg;

        // Kopf zeichnen
        batch.draw(
            headRegion,
            headX,
            headY,
            originX, originY,
            headSize, headSize,
            dir, 1f,
            finalAngle
        );

        float rawAngle = MathUtils.sin(walkCycleTime * LEG_FREQUENCY) * LEG_AMPLITUDE;
        float leftRot = Math.round(rawAngle / LEG_CHUNK_STEP) * LEG_CHUNK_STEP;
        float rightRot = -leftRot;

        // rechter Arm (hinten)
        batch.draw(
            rightarmRegion,
            baseX + (headFlip ? 1 * scale : 2 * scale),
            baseY + 8 * scale,
            1.5f * scale, 10 * scale,
            3 * scale * 1.2f, 10 * scale * 1.2f,
            dir, 1f,
            rightRot * 1.5f * -dir
        );

        // Körper
        batch.draw(
            bodyRegion,
            baseX + (headFlip ? 6 * scale : 0),
            baseY + 10 * scale,
            0f, 0f,
            6 * scale, 12 * scale,
            dir, 1f,
            0
        );

        // linkes Bein
        batch.draw(
            leftlegRegion,
            baseX + (headFlip ? 3 * scale : 0),
            baseY,
            1.5f * scale, 10 * scale,
            3 * scale, 10 * scale,
            dir, 1f,
            -leftRot
        );

        // rechtes Bein
        batch.draw(
            rightlegRegion,
            baseX + (headFlip ? 0 : 3 * scale),
            baseY,
            1.5f * scale, 10 * scale,
            3 * scale, 10 * scale,
            dir, 1f,
            -rightRot
        );

        // linker Arm (vorne)
        batch.draw(
            leftarmRegion,
            baseX + (headFlip ? 1 * scale : 2 * scale),
            baseY + 8 * scale,
            1.5f * scale, 10 * scale,
            3 * scale * 1.2f, 10 * scale * 1.2f,
            dir, 1f,
            rightRot * 1.5f * dir
        );
    }

    public boolean facingLeft() {
        float scale = GameScreen.TILE_SIZE / 16f;
        float baseX = x * GameScreen.TILE_SIZE;

        // Mausposition in Weltkoords
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        // Kopf-Dimensionen & Offset
        float headSize   = 8 * scale;
        float headOffsetX= flip
            ? bodyRegion.getRegionWidth() + 4*scale - headSize
            : -1 * scale;
        float headOffsetY = 22 * scale;

        // Position unten links zum Zeichnen
        float headX = baseX + headOffsetX;

        // Pivot unten-Mitte
        float originX = headSize / 2f;
        float pivotX  = headX + originX;

        // Spiegelflage: wenn Cursor links vom Pivot
        return mouse.x < pivotX;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
