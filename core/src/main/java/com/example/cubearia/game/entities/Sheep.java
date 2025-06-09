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

import static com.example.cubearia.game.GameScreen.TILE_SIZE;

/**
 * Ein Spieler‐Entity, das sich um Bewegung, Kollision,
 * Blockabbau/-platzierung und Animation kümmert.
 */
public class Sheep extends Entity {
    private final OrthographicCamera camera;
    private Player player;

    private float velocityX = 0f;
    private float velocityY = 0f;

    private final float gravity = -40f;
    private final float jumpVelocity = (float) (10f * Math.sqrt(size));
    private final float moveAcceleration = 30f * size;
    private final float airControlFactor = 0.4f;
    private final float friction = 100f;
    private final float finalmaxSpeed = 2f * size;

    public boolean onGround = false;
    private boolean inFluid = false;

    private float stateTime = 0f;
    private float walkCycleTime = 0f;
    private boolean facingLeft = false;
    int aiAction = -1;
    double aiActionTimer = 0;
    int targetX = 0;
    int targetY = 0;

    private final TextureRegion headRegion;
    private final TextureRegion bodyRegion;
    private final TextureRegion leftlegRegion;
    private final TextureRegion rightlegRegion;

    private static final int size = 1;

    private static boolean hasAI = false;

    private static final float LEG_AMPLITUDE = 20f;
    private static final float LEG_FREQUENCY = 15f / size;
    private static final float LEG_CHUNK_STEP = 7f;

    private final World world;

    public Sheep(World world, OrthographicCamera camera, Player player, boolean hasAI) {
        super(0, Chunk.HEIGHT, 1f * size, 0.875f * size);
        this.world = world;
        this.camera = camera;
        this.player = player;
        Sheep.hasAI = hasAI;

        // TextureRegions initialisieren
        headRegion      = new TextureRegion(Assets.sheepTexture,    10 * size,  9 * size,   9 * size,   8 * size);
        bodyRegion      = new TextureRegion(Assets.sheepTexture,    0 * size,   0 * size,   18 * size,  9 * size);
        leftlegRegion   = new TextureRegion(Assets.sheepTexture,    0 * size,   9 * size,   5 * size,   8 * size);
        rightlegRegion  = new TextureRegion(Assets.sheepTexture,    5 * size,   9 * size,   5 * size,   8 * size);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        boolean left  = false;
        boolean right = false;
        boolean jump = false;

        if (hasAI) {
            processAI(delta);

            if (aiAction == 0) {
                left = true;
            }
            if (aiAction == 1) {
                right = true;
            }
            if (aiAction == 2) {
                targetX = (int) ((player.getX() + player.width / 2) * 16);
                targetY = (int) ((player.getY() + player.height * 9/10) * 16);
            }

            if (canJumpOverObstacle(left, right)) jump = true;
        }

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
            if (left)  velocityX -= accel * (facingLeft ? 1f : 0.5f);
            if (right) velocityX += accel * (facingLeft ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta);
        } else {
            if (left)  velocityX -= airAccel * (facingLeft ? 1f : 0.5f);
            if (right) velocityX += airAccel * (facingLeft ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta / 10);
        }

        float maxSpeed = onGround ? finalmaxSpeed : inFluid ? finalmaxSpeed * 0.5f : finalmaxSpeed * 1.2f;
        // maxSpeed = ((facingLeft) == (left)) ? maxSpeed : maxSpeed * 0.5f;
        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        if ((jump)) {
            if (onGround) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inFluid) {
                velocityY += 0.5f;
            }
        }

        if (inFluid) {
            velocityY += gravity * 0.2f * delta;
            if (world.getBlock((int) x, Math.round(y + 0.25f)) == BlockType.WATER) {
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

    private boolean canJumpOverObstacle(boolean left, boolean right) {
        int dir = right ? 1 : (left ? -1 : 0);
        int tx = (int) ((x + width / 2)) + dir;
        int ty = (int) (y);
        return world.getBlock(tx, ty).isSolid()
            && !world.getBlock(tx, ty + 1).isSolid()
            && !world.getBlock(tx, ty + 2).isSolid();
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

    // Render-methoden für den Spieler
    @Override
    public void render(SpriteBatch batch) {
        renderAt(batch, x, y, size, targetX, targetY);
    }

    public void renderAt(SpriteBatch batch, float x, float y, int size, int targetX, int targetY) {
        float scale = (TILE_SIZE / 16f) * size;
        float baseX = x * TILE_SIZE;
        float baseY = y * TILE_SIZE;

        float offsetY = 4 * scale; // 4 Pixel nach unten verschieben (Kopf + Körper)

        boolean headFlip = !facingLeft;
        float dir = headFlip ? -1f : 1f;

        // Kopf
        float headX = baseX + (headFlip ? (bodyRegion.getRegionHeight() * 3) / 4f : (bodyRegion.getRegionHeight() * -3) / 4f);
        float headY = (baseY + headRegion.getRegionHeight() * 1/2f * scale - offsetY) + 10;
        float originX = headRegion.getRegionWidth() * scale;
        float originY = headRegion.getRegionHeight() / 2f * scale;
        float pivotX = headX + originX;
        float pivotY = headY + originY;

        // Winkel zu globalen Koordinaten targetX, targetY:
        float angleRad = MathUtils.atan2(targetY - pivotY, targetX - pivotX);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;
        float finalAngle = headFlip ? angleDeg : 180f + angleDeg;

        batch.draw(
            headRegion,
            headX, headY,
            originX, originY,
            headRegion.getRegionWidth() * scale, headRegion.getRegionHeight() * scale,
            dir, 1f,
            finalAngle
        );

        // Beinrotation
        float rawAngle = MathUtils.sin(walkCycleTime * LEG_FREQUENCY) * LEG_AMPLITUDE;
        float leftRot = Math.round(rawAngle / LEG_CHUNK_STEP) * LEG_CHUNK_STEP;
        float rightRot = -leftRot;

        // Körper
        float bodyX = baseX + (headFlip ? bodyRegion.getRegionWidth() * scale : 0);
        float bodyY = baseY + 10 * scale - offsetY;

        batch.draw(
            bodyRegion,
            bodyX, bodyY,
            0f, 0f,
            bodyRegion.getRegionWidth() * scale, bodyRegion.getRegionHeight() * scale,
            dir, 1f,
            0f
        );

        // Linkes Bein
        float leftLegX = baseX + (headFlip ? (bodyRegion.getRegionWidth() / 1.5f) * scale : 1);
        float legY = baseY;

        batch.draw(
            leftlegRegion,
            leftLegX, legY,
            (float) leftlegRegion.getRegionWidth() / 2 * scale, (leftlegRegion.getRegionHeight() * 3f / 4f) * scale,
            leftlegRegion.getRegionWidth() * scale, leftlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -leftRot
        );

        // Rechtes Bein
        float rightLegX = baseX + (headFlip ? 1 : (bodyRegion.getRegionWidth() / 1.5f) * scale);

        batch.draw(
            rightlegRegion,
            rightLegX, legY,
            (float) rightlegRegion.getRegionWidth() / 2 * scale, (rightlegRegion.getRegionHeight() * 3f / 4f) * scale,
            rightlegRegion.getRegionWidth() * scale, rightlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -rightRot
        );
    }

    private void processAI(float delta) {
        aiActionTimer -= delta;
        System.out.println(aiActionTimer);

        facingLeft = ((float) targetX / 16) < (x + width / 2);

        if (aiActionTimer <= 0) {
            if (player.isInRange((int) x, (int) y)) {
                randomStroll(delta, 3);
            } else {
                randomStroll(delta, 2);
            }

            if (aiActionTimer > 0) {
                if (aiAction == 0) {
                    targetX = (int) ((x - (10 + Math.random() * 30)) * 16);
                    targetY = (int) ((y + (5 - (Math.random() * 10))) * 16);
                } else if (aiAction == 1) {
                    targetX = (int) ((x + (10 + Math.random() * 30)) * 16);
                    targetY = (int) ((y + (5 - (Math.random() * 10))) * 16);
                }
            }
        }
    }

    /**
     * processes actions of the animal. Following actions exist:
     * 0 → right
     * 1 → left
     * 2 → look in random direction
     *
     * @param delta delta time since last frame
     */
    private void randomStroll(float delta, int actions) {
        if (Math.random() <= 0.2 * delta) {    // Durch das mal delta verhindern wir die erhöhte Wahrscheinlichkeit bei mehr Frames. 0,2 ≈ 5^-1 → 1 ~ 5 sekunden
            int min = 2;
            int max = 7;

            double randomValue = Math.random();
            for (int i = 0; i < actions; i++) { // So wird immer eine der zufälligen actions ausgewählt.
                if (randomValue <= (double) (i + 1) / actions) {
                    aiAction = i;
                    aiActionTimer = min + Math.random() * (max - min);
                    break;
                }
            }
        }
    }
}
