package com.example.cubearia.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.World;

import static com.example.cubearia.game.GameScreen.TILE_SIZE;

import java.util.List;

/**
 * Ein Spieler‐Entity, das sich um Bewegung, Kollision,
 * Blockabbau/-platzierung und Animation kümmert.
 */
public class Sheep extends Entity {
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

    public Sheep(World world, Player player, boolean hasAI) {
        super(0, Chunk.HEIGHT, 0.9f * size, 0.875f * size);
        this.world = world;
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

            // Improved movement logic based on target position
            if (aiAction == 0) {
                left = true;
            } else if (aiAction == 1) {
                right = true;
            } else if (aiAction == 2) {
                // Look around or at player - no movement
            } else if (aiAction == 3) {
                // Eating - no movement
            } else {
                // Smart movement towards target
                float targetWorldX = targetX / 16f;
                float targetWorldY = targetY / 16f;

                float dx = targetWorldX - x;
                float dy = targetWorldY - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0.5f) { // Only move if target is far enough
                    // Determine movement direction
                    if (Math.abs(dx) > 0.3f) {
                        if (dx > 0) {
                            right = true;
                        } else {
                            left = true;
                        }
                    }
                }
            }

            if (((left || right) && isBlockInFront(left, right)) || isInFluid(world)) {
                jump = true;
            }
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
        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        if (jump) {
            if (onGround) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inFluid) {
                velocityY += 0.5f;
            }
        }

        if (inFluid) {
            velocityY += gravity * 0.2f * delta;
            velocityY = MathUtils.clamp(velocityY, -3f, 3f);
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
                if (world.getBlock(tx, ty, 0).isSolid()) return true;
            }
        }
        return false;
    }

    private boolean isBlockInFront(boolean left, boolean right) {
        int dir = right ? 1 : (left ? -1 : 0);
        if (dir == 0) return false;

        int tx = (int) (x + width / 2) + dir;
        int ty = (int) y;

        // Check if there's a solid block in front
        if (!world.getBlock(tx, ty, 0).isSolid()) {
            return false;
        }

        // Check if there's space above to jump over (1 block high is enough for sheep)
        if (world.getBlock(tx, ty + 1, 0).isSolid()) {
            return false;
        }

        // Only jump if we're on ground
        return onGround;
    }

    private boolean shouldLookAtPlayer() {
        float distanceToPlayer = getDistanceToPlayer();
        // Much more frequent observation when player is nearby
        return distanceToPlayer < 20f && distanceToPlayer > 1f && Math.random() < 0.6;
    }

    private void lookAtPlayer() {
        aiAction = 2; // Look around action, but we'll override the target
        aiActionTimer = 1.5f + (float) Math.random() * 2f; // Shorter but more frequent observations

        // Set target to player position for head rotation
        targetX = (int) (player.getX() * 16);
        targetY = (int) (player.getY() * 16);
    }

    private void checkFluidState() {
        inFluid = isInFluid(world);
    }

    /**
     * Findet die Wasseroberfläche über dem Schaf
     * Gibt die Y-Koordinate der Wasseroberfläche zurück
     */
    private float findWaterSurface() {
        int currentY = (int) y;

        // Suche nach oben bis zur Wasseroberfläche
        for (int y = currentY; y < 512; y++) {
            if (!world.getBlock((int) x, y, 1).isFluid()) {
                // Wir haben die Wasseroberfläche gefunden
                return y - 0.1f; // Etwas unter der Oberfläche
            }
        }

        // Fallback: Aktuelle Position
        return currentY;
    }

    /**
     * Schwimmt zur Wasseroberfläche
     */
    private void swimToSurface(float delta) {
        float waterSurface = findWaterSurface();
        float distanceToSurface = waterSurface - y;

        if (distanceToSurface > 0.5f) {
            // Schwimme zur Oberfläche
            velocityY += 2.0f * delta;
        } else if (distanceToSurface < -0.5f) {
            // Zu hoch über der Oberfläche, sinke etwas
            velocityY -= 1.0f * delta;
        } else {
            // An der Oberfläche, halte Position
            velocityY *= 0.9f; // Dämpfung
        }
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
        float headY = (baseY + headRegion.getRegionHeight() / 2f * scale - offsetY) + 10;
        float originX = headRegion.getRegionWidth() * scale;
        float originY = headRegion.getRegionHeight() / 2f * scale;
        float pivotX = headX + originX;
        float pivotY = headY + originY;

        // Winkel zu globalen Koordinaten targetX, targetY:
        float angleDeg;
        float finalAngle;
        if (aiAction == 3) {
            angleDeg = (MathUtils.sin(stateTime * 15) * 20) + 35;
            finalAngle = headFlip ? angleDeg + 90 : angleDeg;
        } else {
            float angleRad = MathUtils.atan2(targetY - pivotY, targetX - pivotX);
            angleDeg = angleRad * MathUtils.radiansToDegrees;
            finalAngle = headFlip ? angleDeg : 180f + angleDeg;
        }

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

        batch.draw(
            leftlegRegion,
            leftLegX, baseY,
            (float) leftlegRegion.getRegionWidth() / 2 * scale, (leftlegRegion.getRegionHeight() * 3f / 4f) * scale,
            leftlegRegion.getRegionWidth() * scale, leftlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -leftRot
        );

        // Rechtes Bein
        float rightLegX = baseX + (headFlip ? 1 : (bodyRegion.getRegionWidth() / 1.5f) * scale);

        batch.draw(
            rightlegRegion,
            rightLegX, baseY,
            (float) rightlegRegion.getRegionWidth() / 2 * scale, (rightlegRegion.getRegionHeight() * 3f / 4f) * scale,
            rightlegRegion.getRegionWidth() * scale, rightlegRegion.getRegionHeight() * scale,
            dir, 1f,
            -rightRot
        );
    }

    private void processAI(float delta) {
        aiActionTimer -= delta;

        // Update facing direction based on movement
        if (velocityX != 0) {
            facingLeft = velocityX < 0;
        }

        if (aiActionTimer <= 0) {
            // Check for different behaviors in priority order
            if (shouldFleeFromPlayer()) {
                fleeFromPlayer();
            } else if (shouldLookAtPlayer()) {
                lookAtPlayer();
            } else if (shouldSeekFood()) {
                seekFood();
            } else if (shouldFollowHerd()) {
                followHerd();
            } else if (shouldEat()) {
                eat();
            } else {
                randomStroll(delta, 3);
            }

            // Set target positions for movement
            if (aiActionTimer > 0) {
                if (aiAction == 0) { // Move left
                    targetX = (int) ((x - (10 + Math.random() * 30)) * 16);
                    targetY = (int) ((y + (5 - (Math.random() * 10))) * 16);
                } else if (aiAction == 1) { // Move right
                    targetX = (int) ((x + (10 + Math.random() * 30)) * 16);
                    targetY = (int) ((y + (5 - (Math.random() * 10))) * 16);
                } else if (aiAction == 2) { // Look around or at player
                    // Target is already set by the specific behavior
                    if (targetX == 0 && targetY == 0) {
                        targetX = (int) ((x + (Math.random() - 0.5) * 20) * 16);
                        targetY = (int) ((y + (Math.random() - 0.5) * 20) * 16);
                    }
                } else if (aiAction == 3) { // Eating animation
                    targetX = (int) ((x) * 16);
                    targetY = (int) ((y) * 16);
                }
            }
        }
    }

    /**
     * Checks if the sheep should flee from the player
     */
    private boolean shouldFleeFromPlayer() {
        if (!player.isInRange((int) x, (int) y)) {
            return false;
        }

        float distanceToPlayer = getDistanceToPlayer();
        return distanceToPlayer < 8f; // Flee if player is within 8 blocks
    }

    /**
     * Makes the sheep flee from the player
     */
    private void fleeFromPlayer() {
        float playerX = player.getX();
        float playerY = player.getY();

        // Calculate direction away from player
        float dx = x - playerX;
        float dy = y - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            // Normalize and scale to flee distance
            dx = (dx / distance) * 20f;
            dy = (dy / distance) * 10f;

            // Set target away from player
            targetX = (int) ((x + dx) * 16);
            targetY = (int) ((y + dy) * 16);

            // Set movement direction
            aiAction = dx < 0 ? 0 : 1; // 0 = left, 1 = right
            aiActionTimer = 2f + (float) Math.random() * 3f;
        }
    }

    /**
     * Checks if the sheep should seek food (grass blocks)
     */
    private boolean shouldSeekFood() {
        // Check if there's grass nearby
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int checkX = (int) x + dx;
                int checkY = (int) y + dy;
                if (world.getBlock(checkX, checkY, 0) == BlockType.GRASSBLOCK) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Makes the sheep seek nearby grass blocks
     */
    private void seekFood() {
        float closestDistance = Float.MAX_VALUE;
        int closestX = 0, closestY = 0;

        // Find closest grass block
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                int checkX = (int) x + dx;
                int checkY = (int) y + dy;
                if (world.getBlock(checkX, checkY, 0) == BlockType.GRASSBLOCK) {
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestX = checkX;
                        closestY = checkY;
                    }
                }
            }
        }

        if (closestDistance < Float.MAX_VALUE) {
            targetX = (int) (closestX * 16);
            targetY = (int) (closestY * 16);

            // Set movement direction
            aiAction = closestX < x ? 0 : 1; // 0 = left, 1 = right
            aiActionTimer = 1f + (float) Math.random() * 2f;
        }
    }

    /**
     * Checks if the sheep should follow other sheep (herd behavior)
     */
    private boolean shouldFollowHerd() {
        List<Entity> entities = world.getEntities();
        int nearbySheep = 0;
        float avgX = 0, avgY = 0;

        for (Entity e : entities) {
            if (e instanceof Sheep && e != this) {
                float distance = getDistanceToEntity(e);
                if (distance < 15f) { // Within 15 blocks
                    nearbySheep++;
                    avgX += e.getX();
                    avgY += e.getY();
                }
            }
        }

        if (nearbySheep > 0) {
            avgX /= nearbySheep;
            avgY /= nearbySheep;

            // Move towards herd center if not too close
            float distanceToHerd = (float) Math.sqrt((x - avgX) * (x - avgX) + (y - avgY) * (y - avgY));
            return distanceToHerd > 5f && distanceToHerd < 20f;
        }

        return false;
    }

    /**
     * Makes the sheep follow the herd
     */
    private void followHerd() {
        List<Entity> entities = world.getEntities();
        float avgX = 0, avgY = 0;
        int count = 0;

        for (Entity e : entities) {
            if (e instanceof Sheep && e != this) {
                float distance = getDistanceToEntity(e);
                if (distance < 15f) {
                    avgX += e.getX();
                    avgY += e.getY();
                    count++;
                }
            }
        }

        if (count > 0) {
            avgX /= count;
            avgY /= count;

            targetX = (int) (avgX * 16);
            targetY = (int) (avgY * 16);

            aiAction = avgX < x ? 0 : 1; // 0 = left, 1 = right
            aiActionTimer = 1.5f + (float) Math.random() * 2f;
        }
    }

    /**
     * Checks if the sheep should eat (standing on grass)
     */
    private boolean shouldEat() {
        return world.getBlock((int) x, (int) (y - 1), 0) == BlockType.GRASSBLOCK && Math.random() <= 0.15;
    }

    /**
     * Makes the sheep eat (standing still animation)
     */
    private void eat() {
        aiAction = 3; // Eating animation
        aiActionTimer = 3f + (float) Math.random() * 2f;
        targetX = (int) (x * 16);
        targetY = (int) (y * 16);
    }

    /**
     * Gets distance to player
     */
    private float getDistanceToPlayer() {
        float dx = x - player.getX();
        float dy = y - player.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Gets distance to another entity
     */
    private float getDistanceToEntity(Entity entity) {
        float dx = x - entity.getX();
        float dy = y - entity.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
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
