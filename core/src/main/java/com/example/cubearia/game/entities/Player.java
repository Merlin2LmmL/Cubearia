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
import com.example.cubearia.game.entities.particle.Particle;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemRegistry;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.World;

/**
 * Ein Spieler‐Entity, das sich um Bewegung, Kollision,
 * Blockabbau/-platzierung und Animation kümmert.
 */
public class Player extends Entity {
    private final OrthographicCamera camera;
    private final Inventory inventory;

    private float velocityX = 0f;
    private float velocityY = 0f;

    private final float gravity = -40f;
    private final float jumpVelocity = (float) (10f * Math.sqrt(size));
    private final float moveAcceleration = 30f * size;
    private final float airControlFactor = 0.4f;
    private final float friction = 100f;
    private float finalmaxSpeed = 6f * size;
    private final int range = 5 * size;

    private int selectedSlot = 0;
    private boolean inventoryOpen;
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

    private static int size = 1;

    private static final float LEG_AMPLITUDE = 20f;
    private static final float LEG_FREQUENCY = 10f / size;
    private static final float LEG_CHUNK_STEP = 7f;

    private final World world;

    // Felder für Block‐Breaking
    private float breakingTimer = 0f;
    private float breakingTimeTotal = 0f;
    private int breakingX = -1, breakingY = -1;
    private boolean isBreaking = false;

    public Player(World world, OrthographicCamera camera, Inventory inventory) {
        super(0, Chunk.HEIGHT, 0.375f * size, 1.82f * size);
        this.world = world;
        this.camera = camera;
        this.inventory = inventory;

        // Beispiel‐Starter‐Items
        inventory.addItem(Item.valueOf("WATER"), 64 * 4);
        inventory.addItem(Item.valueOf("OAK_LOG"), 64 * 4);
        inventory.addItem(Item.valueOf("COBBLESTONE"), 64 * 4);

        // TextureRegions initialisieren
        headRegion    = new TextureRegion(Assets.playerTexture,      6 * size,   0 * size,   8 * size, 8 * size);
        bodyRegion    = new TextureRegion(Assets.playerTexture,      0 * size,   0 * size,   6 * size, 12 * size);
        leftlegRegion = new TextureRegion(Assets.playerTexture,      0 * size,  12 * size,  3 * size, 10 * size);
        rightlegRegion= new TextureRegion(Assets.playerTexture,      3 * size,  12 * size,  3 * size, 10 * size);
        leftarmRegion = new TextureRegion(Assets.playerTexture,      6 * size,   8 * size,   4 * size, 11 * size);
        rightarmRegion= new TextureRegion(Assets.playerTexture,     10 * size,   8 * size,   4 * size, 11 * size);
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
            left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            for (int i = 0; i <= 9; i++) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0 + i)) {
                    selectedSlot = i - 1;
                }
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

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.SPACE))) {
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

        // Umgebung nachladen
        world.updateAroundPlayer(x);

        norminatedHight = 0.3f * (float) Math.pow((double) (world.getBiomeTypeAt((int) y).getTemperatureValue()), 2)
            - ((y - Settings.SEA_LEVEL) / (Settings.SEA_LEVEL));
        norminatedHight = MathUtils.clamp(norminatedHight, 0, 1);
        temperatureDamage = (2 / (1 - norminatedHight) - 2);
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

    public boolean isInRange(int blockX, int blockY) {
        float dx = blockX - x;
        float dy = blockY - y;
        return dx*dx + dy*dy <= range*range;
    }

    public boolean intersects(int globalX, int globalY) {
        if (x >= 0) {
            return (globalX == (int) x && globalY == (int) y + 1)
                || (globalX == (int) x && globalY == (int) y);
        } else {
            return (globalX == (int) x - 1 && globalY == (int) y + 1)
                || (globalX == (int) x - 1 && globalY == (int) y);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTemperatureDamage() {
        return temperatureDamage;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    /**
     * Versucht, den Block unter dem Cursor abzubauen.
     * GIBT ein DroppedItem zurück, falls tatsächlich ein Drop entstehen soll,
     * andernfalls null.
     */
    public DroppedItem handleBlockBreaking() {
        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();
        BlockType block = getBlockAtCursor();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (isInRange(globalX, globalY) && block.isBreakable()) {
                float toolLevel = inventory.getSlot(selectedSlot) != null
                    ? inventory.getSlot(selectedSlot).getItem().getToolLevel()
                    : 0;
                float toolType = inventory.getSlot(selectedSlot) != null
                    ? inventory.getSlot(selectedSlot).getItem().getType()
                    : 0;
                float preferredTool = block.getPreferredTool();
                float minLevel  = block.getMinLevel();
                float requiredToolType  = block.getPreferredTool();
                float hardness  = block.getHardness();

                // a) Neuer Abbauvorgang
                if (!isBreaking || globalX != breakingX || globalY != breakingY) {
                    breakingX = globalX;
                    breakingY = globalY;

                    if (toolLevel >= minLevel && toolType == requiredToolType) {
                        breakingTimeTotal = hardness / (2f + (toolLevel - minLevel) * 5f);
                    } else {
                        breakingTimeTotal = hardness * 0.5f;
                    }
                    if (breakingTimeTotal <= 0f) breakingTimeTotal = 0.1f;
                    breakingTimer = 0f;
                    isBreaking = true;
                    return null;
                }

                // b) Fortlaufender Abbau
                breakingTimer += Gdx.graphics.getDeltaTime();

                // Particles
                if (Math.random() < 0.01f) {
                    TextureRegion blockTex = block.getRegion();
                    int texWidth = blockTex.getRegionWidth();
                    int texHeight = blockTex.getRegionHeight();

                    float spawnX = breakingX + 0.5f + MathUtils.random(-0.6f, 0.6f);
                    float spawnY = breakingY + 0.5f + MathUtils.random(-0.6f, 0.6f);
                    float velX = MathUtils.random(-1f, 1f);
                    float velY = MathUtils.random(0.5f, 2f);

                    int px = MathUtils.random(0, texWidth - 2);
                    int py = MathUtils.random(0, texHeight - 2);

                    TextureRegion pixelTex = new TextureRegion(blockTex.getTexture(),
                        blockTex.getRegionX() + px,
                        blockTex.getRegionY() + py,
                        2, 2
                    );

                    Particle particle = new Particle(spawnX, spawnY, velX, velY, 0.3f, pixelTex);
                    world.spawnParticle(particle);
                }

                if (breakingTimer >= breakingTimeTotal || Settings.CREATIVE_MODE) {
                    world.setBlock(breakingX, breakingY, BlockType.AIR);

                    DroppedItem drop = null;
                    String dropId = block.getDroppingItem();
                    if (dropId != null && !dropId.isEmpty()) {
                        Item dropping_item = Item.valueOf(dropId);
                        if (dropping_item != null
                            && (block.getMinLevel() == 0
                            || (preferredTool == toolType && minLevel <= toolLevel))) {
                            drop = new DroppedItem(
                                world,
                                breakingX,
                                breakingY + 0.25f,
                                dropping_item,
                                MathUtils.random(5f, 8f) * (MathUtils.randomBoolean() ? 1 : -1),
                                MathUtils.random(2f, 3f),
                                1
                            );
                        }
                    }

                    world.blockUpdate(breakingX, breakingY, true);

                    isBreaking = false;
                    return drop;
                }
            } else {
                isBreaking = false;
                return null;
            }
        } else {
            isBreaking = false;
            return null;
        }
        return null;
    }

    /**
     * Versucht, den Block unter dem Cursor zu platzieren.
     * Wird direkt ausgeführt, ohne Drop-Rückgabe.
     */
    public void handleBlockPlacing() {
        if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) return;

        int globalX = getBlockGlobalXAtCursor();
        int globalY = getBlockGlobalYAtCursor();

        if (!isInRange(globalX, globalY)) return;
        if (world.getBlock(globalX, globalY) != BlockType.AIR
            && world.getBlock(globalX, globalY) != BlockType.WATER) return;
        if (intersects(globalX, globalY)) return;

        int slotIndex = selectedSlot;
        ItemStack stack = inventory.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return;

        BlockType blockToPlace = BlockType.valueOf(stack.getItem().getId().toUpperCase());
        world.setBlock(globalX, globalY, blockToPlace);
        world.blockUpdate(globalX, globalY, true);

        stack.decrease(1);
        if (stack.isEmpty()) {
            inventory.removeItem(slotIndex);
        }
    }

    /** Gibt zurück, ob gerade abgebaut wird. */
    public boolean isBreaking() {
        return isBreaking;
    }

    /** X‐Koordinate des aktuell abgebauten Blocks (oder –1). */
    public int getBreakingX() {
        return breakingX;
    }

    /** Y‐Koordinate des aktuell abgebauten Blocks (oder –1). */
    public int getBreakingY() {
        return breakingY;
    }

    /**
     * Gibt das TextureRegion für die Abbau-Animation zurück.
     * Falls breakingTimeTotal ≤ 0, wird sicherheitshalber Frame 0 genommen.
     */
    public TextureRegion getBreakingFrame() {
        if (breakingTimeTotal <= 0f) {
            return Assets.breakingFrames[0];
        }
        float progress = breakingTimer / breakingTimeTotal;
        int frameIndex = Math.min((int)(progress * 4), 3);
        return Assets.breakingFrames[frameIndex];
    }

    // Hilfsmethoden, um aus Mauskoordianten Blockkoordinaten zu berechnen
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
            mouse.y - pivotY - 4 * scale,
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
