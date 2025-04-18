package com.example.cubearia.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Player extends Entity {
    private final OrthographicCamera camera;
    private final Inventory inventory;

    public float velocityX = 0f;
    public float velocityY = 0f;

    public final float gravity = -25f;
    public final float jumpVelocity = 7.5f;
    public final float moveAcceleration = 30f;
    public final float airControlFactor = 0.4f;
    public final float friction = 100f;
    public final float maxSpeed = 6f;
    public final int range = 5;


    public int selectedSlot = 0;

    public boolean onGround = false;
    private boolean inWater = false;

    private float stateTime = 0f;
    private float walkCycleTime = 0f;
    private boolean flip = false;

    private final TextureRegion headRegion;
    private final TextureRegion bodyRegion;
    private final TextureRegion leftlegRegion;
    private final TextureRegion rightlegRegion;
    private final TextureRegion leftarmRegion;
    private final TextureRegion rightarmRegion;

    private static final float LEG_AMPLITUDE = 20f;       // Maximum rotation angle
    private static final float LEG_FREQUENCY = 10f;       // Speed of cycle
    private static final float LEG_CHUNK_STEP = 7f;      // Chunky rotation step

    private final World world;

    public Player(World world, OrthographicCamera camera, Inventory inventory) {
        super(0, 128, 0.385f, 1.82f);
        this.world = world;
        this.camera = camera;
        this.inventory = inventory;
        // Prepare texture regions
        headRegion = new TextureRegion(Assets.playerTexture, 6, 0, 8, 8);
        bodyRegion = new TextureRegion(Assets.playerTexture, 0, 0, 6, 12);
        leftlegRegion  = new TextureRegion(Assets.playerTexture, 0, 12, 3, 10);
        rightlegRegion  = new TextureRegion(Assets.playerTexture, 3, 12, 3, 10);
        leftarmRegion  = new TextureRegion(Assets.playerTexture, 6, 8, 4, 11);
        rightarmRegion  = new TextureRegion(Assets.playerTexture, 10, 8, 4, 11);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        checkWaterState();

        // Walk Cycle
        if (velocityX != 0) {
            walkCycleTime += Math.abs(velocityX) * delta * (onGround ? 0.3f : 0.1f);
        } else {
            walkCycleTime = 0f;
        }

        for (int i = 0; i <= 9; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
                selectedSlot = 9;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                selectedSlot = i - 1;
            }
        }

        float accel = moveAcceleration * delta;
        float airAccel = accel * airControlFactor;

        if (onGround) {
            if (left) velocityX -= accel;
            if (right) velocityX += accel;
            if (!left && !right) applyFriction(delta);
        } else {
            if (left) velocityX -= airAccel;
            if (right) velocityX += airAccel;
            if (!left && !right) applyFriction(delta / 10);
        }
        if (onGround) velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed); else
        if (inWater) velocityX = MathUtils.clamp(velocityX, -maxSpeed * 0.5f, maxSpeed * 0.5f); else
            velocityX = MathUtils.clamp(velocityX, -maxSpeed * 1.2f, maxSpeed * 1.2f);

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.SPACE))) {
            if (onGround) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inWater) {
                velocityY += 1f;
            }
        }

        if (inWater) {
            velocityY += gravity * 0.2f * delta; // weniger Gravitation im Wasser
            if (world.getBlock((int) x, (int) (Math.round(y + 0.25f))) == BlockType.WATER_SURFACE) {
                velocityY = MathUtils.clamp(velocityY, -4f, 8f); // nicht zu schnell fallen
            } else {
                velocityY = MathUtils.clamp(velocityY, -4f, 4f); // nicht zu schnell fallen
            }
        } else {
            velocityY += gravity * delta;
        }

        // Movement + Kollision
        moveWithCollision(delta);

        // Umgebung ggf. neu laden
        world.updateAroundPlayer(x, (int) camera.zoom + 1);
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
        // Save old pos
        float oldX = x;
        float oldY = y;

        // Move X
        x += velocityX * delta;
        if (checkCollision()) {
            if (velocityX > 0) x = (float)Math.floor(x + width) - width;
            else if (velocityX < 0) x = (float)Math.floor(x + 1);
            velocityX = 0;
        }

        // Move Y
        y += velocityY * delta;
        if (checkCollision()) {
            if (velocityY > 0) y = (float)Math.floor(y + height) - height;
            else if (velocityY < 0) {
                y = (float)Math.floor(y + 1);
                onGround = true;
            }
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

    private void checkWaterState() {
        inWater = world.getBlock((int) x, (int) y) == BlockType.WATER_FULL || world.getBlock((int) x, (int) (Math.round(y + 0.25f))) == BlockType.WATER_SURFACE;
    }

    public boolean isInRange(int blockX, int blockY) {
        return Math.abs(blockX - x) <= range && Math.abs(blockY - y) <= range;
    }

    public void render(SpriteBatch batch) {
        float scale = GameScreen.TILE_SIZE / 16f;
        float baseX = x * GameScreen.TILE_SIZE;
        float baseY = y * GameScreen.TILE_SIZE;


        // Mausposition in Weltkoords
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        // Kopf-Dimensionen & Offset
        float headSize   = 8 * scale;
        float headOffsetX= flip
            ? bodyRegion.getRegionWidth() + 4*scale - headSize
            : -1 * scale;
        float headOffsetY= 22 * scale;

        // Position unten links zum Zeichnen
        float headX = baseX + headOffsetX;
        float headY = baseY + headOffsetY;

        // Pivot unten-Mitte
        float originX = headSize / 2f;
        float originY = 0f;
        float pivotX  = headX + originX;
        float pivotY  = headY + originY;

        // Winkel vom Pivot zum Cursor (±180°)
        float angleRad = (float)Math.atan2(
            mouse.y - pivotY,
            mouse.x - pivotX
        );
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        // Spiegelflage: wenn Cursor links vom Pivot
        boolean headFlip = mouse.x < pivotX;
        float scaleX     = headFlip ? -1f : 1f;
        float dir = headFlip ? -1f : 1f;

        // Winkel anpassen, damit die Rotation ≤180° und optisch korrekt bleibt
        float finalAngle;
        if (headFlip) {
                finalAngle = 180f + angleDeg;
        } else {
            finalAngle = angleDeg;
        }

        // Kopf rendern
        batch.draw(
            headRegion,
            baseX + (flip ? bodyRegion.getRegionWidth() - 4*scale - 3 * scale : -1*scale),
            headY,
            originX, originY,
            headSize, headSize,
            scaleX, 1f,
            finalAngle
        );

        // Compute chunky leg rotations
        float rawAngle = MathUtils.sin(walkCycleTime * LEG_FREQUENCY) * LEG_AMPLITUDE;
        float leftRot = Math.round(rawAngle / LEG_CHUNK_STEP) * LEG_CHUNK_STEP;
        float rightRot = -leftRot;

        // rechter Arm (hinten)
        batch.draw(
            rightarmRegion,
            baseX + (headFlip ? bodyRegion.getRegionWidth() - 2*scale - 3*scale : 2*scale),
            baseY + 8 * scale,
            3*scale/2f,10*scale,
            3*scale * 1.2f, 10*scale * 1.2f,
            dir, 1f,
            0.5f - rightRot * 1.5f * (headFlip ? -1f : 1f)
        );

        // Körper
        batch.draw(
            bodyRegion,
            baseX + (headFlip ? 6 : 0 * scale),
            baseY + 10 * scale,
            0f, 0f,
            6 * scale, 12 * scale,
            dir, 1f,
            0
        );

        // linkes Bein
        batch.draw(
            leftlegRegion,
            baseX + (flip ? bodyRegion.getRegionWidth() - 0*scale - 3*scale : 0*scale),
            baseY,
            3*scale/2f, 10*scale,
            3*scale, 10*scale,
            dir, 1f,
            -leftRot * (headFlip ? -1f : 1f)
        );

        // rechtes Bein
        batch.draw(
            rightlegRegion,
            baseX + (flip ? bodyRegion.getRegionWidth() - 3*scale - 3*scale : 3*scale),
            baseY,
            3*scale/2f, 10*scale,
            3*scale, 10*scale,
            dir, 1f,
            -rightRot * (headFlip ? -1f : 1f)
        );

        // linker Arm (vorne)
        batch.draw(
            leftarmRegion,
            baseX + (flip ? bodyRegion.getRegionWidth() - 2*scale - 3*scale : 2*scale),
            baseY + 8 * scale,
            3*scale/2f,10*scale,
            3*scale * 1.2f, 10*scale * 1.2f,
            dir, 1f,
            0.5f + rightRot * 1.5f * (headFlip ? -1f : 1f)
        );
    }

    public Inventory getInventory() {
        return inventory;
    }
}
