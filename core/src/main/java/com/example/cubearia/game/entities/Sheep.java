package com.example.cubearia.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.GameScreen;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.World;

/**
 * Ein Spieler‐Entity, das sich um Bewegung, Kollision,
 * Blockabbau/-platzierung und Animation kümmert.
 */
public class Sheep extends Entity {
    private final OrthographicCamera camera;

    private float velocityX = 0f;
    private float velocityY = 0f;

    private final float gravity = -40f;
    private final float jumpVelocity = (float) (10f * Math.sqrt(size));
    private final float moveAcceleration = 30f * size;
    private final float airControlFactor = 0.4f;
    private final float friction = 100f;
    private final float finalmaxSpeed = 6f * size;

    public boolean onGround = false;
    private boolean inFluid = false;

    private float stateTime = 0f;
    private float walkCycleTime = 0f;
    private boolean flip = false;


    private final TextureRegion headRegion;
    private final TextureRegion bodyRegion;
    private final TextureRegion leftlegRegion;
    private final TextureRegion rightlegRegion;

    private static final int size = 1;

    private static final float LEG_AMPLITUDE = 20f;
    private static final float LEG_FREQUENCY = 10f / size;
    private static final float LEG_CHUNK_STEP = 7f;

    private final World world;

    public Sheep(World world, OrthographicCamera camera) {
        super(0, Chunk.HEIGHT, 0.375f * size, 1.82f * size);
        this.world = world;
        this.camera = camera;

        // TextureRegions initialisieren
        headRegion      = new TextureRegion(Assets.sheepTexture,    10 * size,  10 * size,      9 * size,   8 * size);
        bodyRegion      = new TextureRegion(Assets.sheepTexture,    0 * size,   0 * size,       19 * size,  10 * size);
        leftlegRegion   = new TextureRegion(Assets.sheepTexture,    0 * size,   10 * size,      5 * size,   10 * size);
        rightlegRegion  = new TextureRegion(Assets.sheepTexture,    5 * size,   10 * size,      5 * size,   10 * size);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        boolean left  = false;
        boolean right = false;

        checkFluidState();

        // Walk‐Cycle
        if (velocityX != 0) {
            walkCycleTime += Math.abs(velocityX) * delta * (onGround ? 0.3f : 0.1f);
        } else {
            walkCycleTime = 0f;
        }

        float accel = moveAcceleration * delta;
        float airAccel = accel * airControlFactor;

        if (onGround) {
            if (left)  velocityX -= accel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += accel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta);
        } else {
            if (left)  velocityX -= airAccel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += airAccel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta / 10);
        }

        float maxSpeed = onGround ? finalmaxSpeed : inFluid ? finalmaxSpeed * 0.5f : finalmaxSpeed * 1.2f;
        maxSpeed = ((facingLeft() ? true : false) == (left ? true : false)) ? maxSpeed : maxSpeed * 0.5f;
        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        if ((false)) {
            if (onGround) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inFluid) {
                velocityY += 0.5f;
            }
        }

        if (inFluid) {
            velocityY += gravity * 0.2f * delta;
            if (world.getBlock((int) x, (int) (Math.round(y + 0.25f))) == BlockType.WATER) {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f);
            } else {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f);
            }
        } else {
            velocityY += gravity * delta;
        }

        // Bewegung + Kollision
        moveWithCollision(delta);
    }

    private void applyFriction(float frictionAmount) {
        if (velocityX > 0) {
            velocityX -= friction * frictionAmount;
            if (velocityX < 0) velocityX = 0;
        } else if (velocityX < 0) {
            velocityX += friction * frictionAmount;
            if (velocityX > 0) velocityX = 0;
        }
    }

    private void moveWithCollision(float delta) {
        float oldX = x, oldY = y;

        // X-Bewegung
        x += velocityX * delta;
        if (checkCollision()) {
            x = oldX;
            velocityX = 0;
        }

        // Y-Bewegung
        y += velocityY * delta;
        if (checkCollision()) {
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    private int toTileIndex(float pixelCoord) {
        return (int)Math.floor(pixelCoord / GameScreen.TILE_SIZE);
    }

    // Render-methoden für den Spieler
    @Override
    public void render(SpriteBatch batch) {
        renderAt(batch, x, y, size);
    }

    public void renderAt(SpriteBatch batch, float x, float y, int size) {
        float scale = (GameScreen.TILE_SIZE / 16f) * size;
        float baseX = x * GameScreen.TILE_SIZE;
        float baseY = y * GameScreen.TILE_SIZE;

        float headSize = (headRegion.getRegionHeight() + headRegion.getRegionWidth()) / 2;
        float headOffsetX = flip
            ? bodyRegion.getRegionWidth() * scale - headSize - 4 * scale
            : -1 * scale;
        float headOffsetY = 11 * scale;

        float headX = baseX + headOffsetX;
        float headY = baseY + headOffsetY;

        float originX = -headRegion.getRegionWidth();
        float originY = -headRegion.getRegionHeight();
        float pivotX = headX + originX;
        float pivotY = headY + originY;

        float angleRad = (float) 0.3f;
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        boolean headFlip = false;
        float dir = headFlip ? -1f : 1f;

        float finalAngle = headFlip ? 180f + angleDeg : angleDeg;

        // Kopf zeichnen
        batch.draw(
            headRegion,
            headX,
            headY,
            originX, originY,
            headRegion.getRegionWidth() * scale, headRegion.getRegionHeight() * scale,
            dir, 1f,
            finalAngle
        );

        float rawAngle = MathUtils.sin(walkCycleTime * LEG_FREQUENCY) * LEG_AMPLITUDE;
        float leftRot = Math.round(rawAngle / LEG_CHUNK_STEP) * LEG_CHUNK_STEP;
        float rightRot = -leftRot;

        // Körper
        batch.draw(
            bodyRegion,
            baseX + (headFlip ? 6 * scale : 0),
            baseY + 10 * scale,
            0f, 0f,
            bodyRegion.getRegionWidth() * scale, bodyRegion.getRegionHeight() * scale,
            dir, 1f,
            0
        );

        // linkes Bein
        batch.draw(
            leftlegRegion,
            baseX + (headFlip ? (bodyRegion.getRegionWidth() / 1.5f) * scale : 1),
            baseY,
            1.5f * scale, 10 * scale,
            leftlegRegion.getRegionWidth() * scale, leftlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -leftRot
        );

        // rechtes Bein
        batch.draw(
            rightlegRegion,
            baseX + (headFlip ? 1 : (bodyRegion.getRegionWidth() / 1.5f) * scale),
            baseY,
            1.5f * scale, 10 * scale,
            rightlegRegion.getRegionWidth() * scale, rightlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -rightRot
        );
    }

    public boolean facingLeft() {
        float scale = GameScreen.TILE_SIZE / 16f;
        float baseX = x * GameScreen.TILE_SIZE;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        float headSize    = 8 * scale;
        float headOffsetX = flip
            ? bodyRegion.getRegionWidth() + 4*scale - headSize
            : -1 * scale;
        float headOffsetY = 22 * scale;

        float headX = baseX + headOffsetX;
        float originX = headSize / 2f;
        float pivotX  = headX + originX;

        return mouse.x < pivotX;
    }
}
