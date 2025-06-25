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
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.Chunk;
import com.example.cubearia.game.world.World;

import java.util.Random;

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
    private final float jumpVelocity = Settings.CREATIVE_MODE ? (float) (30f * Math.sqrt(size)) : (float) (10f * Math.sqrt(size));
    private final float moveAcceleration = Settings.CREATIVE_MODE ? 100f * size : 30f * size;
    private final float airControlFactor = 0.4f;
    private final float friction = Settings.CREATIVE_MODE ? 500f : 100f;
    private float finalmaxSpeed = Settings.CREATIVE_MODE ? 18f * size : 6f * size;
    private final int range = 5 * size;

    private int selectedSlot = 0;
    private int selectedLayer = 0; // 0 = vorderer Layer, 1 = hinterer Layer
    public boolean inMenu;

    public boolean onGround = false;
    private boolean inFluid = false;

    private float stateTime = 0f;
    private float walkCycleTime = 0f;
    private float armSwingTime = 0f;
    private boolean isArmSwinging = false;
    private boolean isBreaking = false;
    private float breakingX = 0f;
    private float breakingY = 0f;

    // Bau-Animation Variablen
    private boolean isBuilding = false;
    private float buildingX = 0f;
    private float buildingY = 0f;
    private float buildingTimer = 0f;
    private float buildingTimeTotal = 0.3f;
    private float buildingAmplitude = 30;
    private float buildingFrequency = 2;

    private float temperatureDamage = 0;

    private static final int maxHealth = 20;
    private double health = maxHealth;

    private final TextureRegion headRegion;
    private final TextureRegion bodyRegion;
    private final TextureRegion leftlegRegion;
    private final TextureRegion rightlegRegion;
    private final TextureRegion leftarmRegion;
    private final TextureRegion rightarmRegion;

    private static final int size = 1;

    private static final float LEG_AMPLITUDE = 20f;
    private static final float LEG_FREQUENCY = 15f / size;
    private static final float LEG_CHUNK_STEP = 7f;

    // Arm-Animation Parameter
    private static final float ARM_SWING_AMPLITUDE = 30f; // Amplitude der Schwingung in Grad
    private static final float ARM_SWING_FREQUENCY = 2f; // Frequenz der Schwingung (Schläge pro Sekunde)
    private static final float ARM_ROTATION_CHUNK_STEP = 7f; // Schrittweite für die Sinus-Schwingung (wie bei LEG_CHUNK_STEP)

    private final World world;

    // Felder für Block‐Breaking
    private float breakingTimer = 0f;
    private float breakingTimeTotal = 0f;

    public Player(World world, OrthographicCamera camera, Inventory inventory) {
        // Call super first with default position, then set safe position
        super(0, Chunk.HEIGHT, 0.375f * size, 1.82f * size);
        this.world = world;
        this.camera = camera;
        this.inventory = inventory;

        // Find a safe spawn position and set it
        int safeSpawnY = world.findSafeSpawnPosition(0);
        this.y = safeSpawnY;

        // Beispiel‐Starter‐Items
        inventory.addItem(Item.valueOf("DIAMOND_PICKAXE"), 1);
        inventory.addItem(Item.valueOf("GRASSBLOCK"), 64 * 4);
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
        stateTime += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            // Teleport to a random position but find a safe spawn height
            float randomX = (float) (Math.random() - 0.5) * 20000;
            int safeSpawnY = world.findSafeSpawnPosition((int) randomX);
            x = randomX;
            y = safeSpawnY;
        }

        // Layer switching with TAB key
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            selectedLayer = (selectedLayer + 1) % 2;
        }

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

        // Arm-Schlag-Animation beim Block-Abbauen
        if (isBreaking) {
            if (!isArmSwinging) {
                isArmSwinging = true;
                armSwingTime = 0f;
            }
            armSwingTime += delta * 8f; // Schnelle Animation
        } else {
            isArmSwinging = false;
            armSwingTime = 0f;
        }

        // Bau-Animation
        if (isBuilding) {
            buildingTimer += delta;
            if (buildingTimer >= buildingTimeTotal) {
                isBuilding = false;
            }
        }

        float accel = moveAcceleration * delta;
        float airAccel = accel * airControlFactor;

        // Sprint-Erkennung
        boolean isSprinting = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        boolean movingInFacingDirection = (left && facingLeft()) || (right && !facingLeft());

        if (onGround) {
            if (left)  velocityX -= accel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += accel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta);
        } else {
            if (left)  velocityX -= airAccel * (facingLeft() ? 1f : 0.5f);
            if (right) velocityX += airAccel * (facingLeft() ? 0.5f : 1f);
            if (!left && !right) applyFriction(delta / 10);
        }

        // Berechne maximale Geschwindigkeit basierend auf Sprint und Richtung
        float maxSpeed = onGround ? finalmaxSpeed : inFluid ? finalmaxSpeed * 0.5f : finalmaxSpeed * 1.2f;

        // Sprint-Bonus nur wenn man in die richtige Richtung schaut
        if (isSprinting && movingInFacingDirection) {
            maxSpeed *= 1.5f; // 50% schneller beim Sprinten
        }

        if (onGround && velocityX != 0) {
            walkParticle(delta);
        }

        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        if ((Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.SPACE))) {
            if (Settings.CREATIVE_MODE) {
                // Creative Mode: Fliegen nach oben
                velocityY = jumpVelocity;
                onGround = false;
            } else if (onGround && !inFluid) {
                velocityY = jumpVelocity;
                onGround = false;
            } else if (inFluid) {
                velocityY += 0.5f;
            }
        }

        // Creative Mode: Fliegen nach unten mit S
        if (Settings.CREATIVE_MODE && Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocityY = -jumpVelocity;
            onGround = false;
        }

        if (inFluid) {
            velocityY += gravity * 0.2f * delta;
            if (world.getBlock((int) x, Math.round(y + 0.25f), 1) == BlockType.WATER) {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f);
            } else {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f);
            }
        } else if (Settings.CREATIVE_MODE) {
            // Creative Mode: Keine Schwerkraft, aber Reibung für sanfte Bewegung
            if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S)) {
                velocityY *= 0.997f; // Sanfte Reibung
            }
        } else {
            velocityY += gravity * delta;
        }

        // Bewegung + Kollision
        moveWithCollision(delta);

        // Umgebung nachladen
        world.updateAroundPlayer(x);

        float norminatedHight = (0.3f * (y / Chunk.HEIGHT) * (float) Math.pow(world.getBiomeTypeAt((int) y).getTemperatureValue(), 2))
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
        if (!Settings.CREATIVE_MODE && checkCollision()) {
            x = oldX;
            velocityX = 0;
        }

        // Y-Bewegung
        y += velocityY * delta;
        if (!Settings.CREATIVE_MODE && checkCollision()) {
            y = oldY;
            if (velocityY < 0) onGround = true;
            velocityY = 0;
        } else {
            onGround = false;
        }
    }

    private void walkParticle(float delta) {
        if (Math.random() < 20 * delta * Math.abs(velocityX)) {
            TextureRegion blockTex = world.getBlock((int)x, (int) y - 1, 0).getRegion();
            int texWidth = blockTex.getRegionWidth();
            int texHeight = blockTex.getRegionHeight();

            float velX = MathUtils.random(-1f, 1f);
            float velY = MathUtils.random(0.5f, 2f);

            int px = MathUtils.random(0, texWidth - 2);
            int py = MathUtils.random(0, texHeight - 2);

            TextureRegion pixelTex = new TextureRegion(blockTex.getTexture(),
                blockTex.getRegionX() + px,
                blockTex.getRegionY() + py,
                2, 2
            );

            Particle particle = new Particle(x, y, velX, velY, 0.3f, pixelTex);
            world.spawnParticle(particle);
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

    private void checkFluidState() {
        inFluid = isInFluid(world);
    }

    public boolean isInRange(int blockX, int blockY) {
        float dx = blockX - x;
        float dy = blockY - y;
        return dx*dx + dy*dy <= range*range;
    }

    public Inventory getInventory() {
        return inventory;
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

    public int getSelectedLayer() {
        return selectedLayer;
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
                    // Verwende die neue layer-aware Block-Breaking-Methode
                    BlockType brokenBlock = world.breakTopBlock((int)breakingX, (int)breakingY);

                    DroppedItem drop = null;
                    String dropId = brokenBlock.getDroppingItem();
                    if (dropId != null && !dropId.isEmpty()) {
                        Item dropping_item = Item.valueOf(dropId);
                        if (dropping_item != null
                            && (brokenBlock.getMinLevel() == 0
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

                    world.blockUpdate((int)breakingX, (int)breakingY, true);

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

        // Prüfe, ob der Platz im gewählten Layer bereits besetzt ist
        if (world.getBlock(globalX, globalY, selectedLayer).isSolid()) return;

        int slotIndex = selectedSlot;
        ItemStack stack = inventory.getSlot(slotIndex);
        if (stack == null || stack.isEmpty()) return;

        // Prüfe, ob das Item ein Block ist (nur Blöcke können platziert werden)
        if (stack.getItem().getType() != 0) return; // Type 0 = Block

        BlockType blockToPlace = BlockType.valueOf(stack.getItem().getId().toUpperCase());

        // Verwende die neue layer-aware Block-Placing-Methode
        boolean placed = world.placeBlock(globalX, globalY, blockToPlace, selectedLayer);

        if (placed) {
            // Starte Bau-Animation
            buildingX = globalX;
            buildingY = globalY;
            buildingTimer = 0f;
            isBuilding = true;

            world.blockUpdate(globalX, globalY, true);

            stack.decrease(1);
            if (stack.isEmpty()) {
                inventory.removeItem(slotIndex);
            }
        }
    }

    /** Gibt zurück, ob gerade abgebaut wird. */
    public boolean isBreaking() {
        return isBreaking;
    }

    /** X‐Koordinate des aktuell abgebauten Blocks (oder –1). */
    public int getBreakingX() {
        return (int)breakingX;
    }

    /** Y‐Koordinate des aktuell abgebauten Blocks (oder –1). */
    public int getBreakingY() {
        return (int)breakingY;
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
        return world.getTopBlock(globalX, globalY);
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
        float headOffsetX = -1 * scale;
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

        // Arm-Animation beim Block-Abbauen und -Bauen
        float armSwingAngle = 0f;
        boolean shouldAnimateBackArm = false;
        float blockDirectionAngle = 0f;

        if (isArmSwinging && isBreaking) {
            // Bestimme die Richtung des Blocks zum Spieler
            float blockX = breakingX;
            float blockY = breakingY;

            // Berechne den Winkel zum Block
            float dx = blockX - x;
            float dy = blockY - y;
            blockDirectionAngle = (float) Math.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;

            // Immer der rechte Arm (backArm) wird für das Abbauen verwendet
            shouldAnimateBackArm = true;

            // Sinusförmige Schwingung für die Arm-Animation - wie bei der Laufanimation
            float rawSwing = MathUtils.sin(armSwingTime * ARM_SWING_FREQUENCY) * ARM_SWING_AMPLITUDE;
            armSwingAngle = Math.round(rawSwing / ARM_ROTATION_CHUNK_STEP) * ARM_ROTATION_CHUNK_STEP;
        } else if (isBuilding) {
            // Bau-Animation: Arm "haut" kurz in Richtung des gebauten Blocks
            float blockX = buildingX;
            float blockY = buildingY;

            // Berechne den Winkel zum Block
            float dx = blockX - x;
            float dy = blockY - y;
            blockDirectionAngle = (float) Math.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;

            // Immer der rechte Arm (backArm) wird für das Bauen verwendet
            shouldAnimateBackArm = true;

            // Kurze "Hau"-Animation: Arm schwingt schnell nach vorne und zurück
            float progress = buildingTimer / buildingTimeTotal;
            float swingProgress = Math.min(progress * 3f, 2f); // Animation in den ersten 1/3 der Zeit
            float swingAngle = MathUtils.sin(swingProgress * MathUtils.PI / buildingFrequency) * buildingAmplitude;

            armSwingAngle = swingAngle;
        }

        // rechter Arm (backArm) - Animation beim Abbauen
        float backArmRotation = rightRot * 1.5f * -dir;
        if (shouldAnimateBackArm) {
            // Arm zeigt in Block-Richtung + sinusförmige Schwingung (addiert, nicht ersetzt)
            backArmRotation = blockDirectionAngle + armSwingAngle;
        }

        batch.draw(
            rightarmRegion,
            baseX + (headFlip ? 1 * scale : 2 * scale),
            baseY + 8 * scale,
            1.5f * scale, 10 * scale,
            3 * scale * 1.2f, 10 * scale * 1.2f,
            dir, 1f,
            backArmRotation
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

        // Item in der Hand des Spielers zeichnen
        renderItem(batch, x, y, size);

        // linker Arm (frontArm) - normale Laufanimation
        float frontArmRotation = rightRot * 1.5f * dir;

        batch.draw(
            leftarmRegion,
            baseX + (headFlip ? 1 * scale : 2 * scale),
            baseY + 8 * scale,
            1.5f * scale, 10 * scale,
            3 * scale * 1.2f, 10 * scale * 1.2f,
            dir, 1f,
            frontArmRotation
        );
    }

    // Item in der Hand des Spielers zeichnen (als letztes)
    public void renderItem(SpriteBatch batch, float x, float y, int size) {
        float scale = (GameScreen.TILE_SIZE / 16f) * size;
        float baseX = x * GameScreen.TILE_SIZE;
        float baseY = y * GameScreen.TILE_SIZE;

        boolean headFlip = facingLeft();
        float dir = headFlip ? -1f : 1f;

        // Berechne Arm-Rotationen (wie in renderAt)
        float rawAngle = MathUtils.sin(walkCycleTime * LEG_FREQUENCY) * LEG_AMPLITUDE;
        float leftRot = Math.round(rawAngle / LEG_CHUNK_STEP) * LEG_CHUNK_STEP;
        float rightRot = -leftRot;

        float backArmRotation = rightRot * 1.5f * -dir;
        float frontArmRotation = rightRot * 1.5f * dir;

        int iconSize = 4;

        // Arm-Animation beim Block-Abbauen
        if (isArmSwinging && isBreaking) {
            float blockX = breakingX;
            float blockY = breakingY;
            float dx = blockX - x;
            float dy = blockY - y;
            float blockDirectionAngle = (float) Math.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;

            float rawSwing = MathUtils.sin(armSwingTime * ARM_SWING_FREQUENCY) * ARM_SWING_AMPLITUDE;
            float armSwingAngle = Math.round(rawSwing / ARM_ROTATION_CHUNK_STEP) * ARM_ROTATION_CHUNK_STEP;

            backArmRotation = blockDirectionAngle + armSwingAngle;
        } else if (isBuilding) {
            // Bau-Animation: Arm "haut" kurz in Richtung des gebauten Blocks
            float blockX = buildingX;
            float blockY = buildingY;
            float dx = blockX - x;
            float dy = blockY - y;
            float blockDirectionAngle = (float) Math.atan2(dy, dx) * MathUtils.radiansToDegrees + 90;

            // Kurze "Hau"-Animation: Arm schwingt schnell nach vorne und zurück
            float progress = buildingTimer / buildingTimeTotal;
            float swingProgress = Math.min(progress * 3f, 2f); // Animation in den ersten 1/3 der Zeit
            float swingAngle = MathUtils.sin(swingProgress * MathUtils.PI / buildingFrequency) * buildingAmplitude;

            backArmRotation = blockDirectionAngle + swingAngle;
        }

        ItemStack heldItem = inventory.getSlot(selectedSlot);
        if (heldItem != null && !heldItem.isEmpty()) {
            TextureRegion itemTexture = heldItem.getItem().getIcon();

            float itemX, itemY, itemRotation;
            float armEndX, armEndY;

            if (isArmSwinging && isBreaking) {
                // Beim Abbauen: Item in der Hand des Arms, der abbaut (backArm)
                armEndX = baseX + (headFlip ? 1 * scale : 2 * scale);
                armEndY = baseY + 8 * scale;
                itemRotation = backArmRotation;
            } else if (isBuilding) {
                // Beim Bauen: Item in der Hand des Arms, der baut (backArm)
                armEndX = baseX + (headFlip ? 1 * scale : 2 * scale);
                armEndY = baseY + 8 * scale;
                itemRotation = backArmRotation;
            } else {
                // Normal: Item in der vorderen Hand (frontArm) für Sichtbarkeit
                armEndX = baseX + (headFlip ? 1 * scale : 2 * scale);
                armEndY = baseY + 8 * scale;
                itemRotation = frontArmRotation;
            }

            // Berechne die exakte Endposition des Arms basierend auf Rotation und Länge
            float armLength = 10 * scale * 1.2f; // Länge des Arms (wie in der Arm-Rendering-Methode)
            float armOriginX = 1.5f * scale;
            float armOriginY = 10 * scale;
            float armOffsetX = -12; // Standard-Offset für normale Items
            float armOffsetY = -2;

            // Berechne die Endposition des Arms
            float armEndOffsetX = armLength * (float) Math.cos(Math.toRadians(itemRotation));
            float armEndOffsetY = armLength * (float) Math.sin(Math.toRadians(itemRotation));

            // Endposition des Arms = Arm-Position + Origin + Offset
            float actualArmEndX = armEndX + armOriginX + armEndOffsetX;
            float actualArmEndY = armEndY + armOriginY + armEndOffsetY;

            if (heldItem.getItem().getToolLevel() != 0) {
                itemRotation += facingLeft() ? 45 : 315; // Konsistente Rotation für Werkzeuge
                iconSize = 8;

                // Für Werkzeuge: Position anpassen, damit das Item trotz Origin-Punkt (0,0) an der richtigen Stelle bleibt
                // UND die Arm-Rotation berücksichtigen
                armOffsetX = facingLeft() ? -15 : -2;
                armOffsetY = facingLeft() ? 10 : -7;
            }

            // Berechne die Position basierend auf der Arm-Rotation
            float cos = (float) Math.cos(Math.toRadians(itemRotation));
            float sin = (float) Math.sin(Math.toRadians(itemRotation));

            actualArmEndX += armOffsetX * cos - armOffsetY * sin;
            actualArmEndY += armOffsetX * sin + armOffsetY * cos;

            // Origin-Punkt für das Item: bei Werkzeugen an der unteren Kante, sonst in der Mitte
            float originX = 1.5f * scale;
            float originY = 10 * scale;
            if (heldItem.getItem().getToolLevel() != 0) {
                originX = 0;
                originY = iconSize * scale; // Untere Kante des Items
            }

            // Item zeichnen - Origin-Punkt exakt an der Arm-Endposition
            batch.draw(
                itemTexture,
                actualArmEndX - originX, // Position so anpassen, dass Origin-Punkt an der Arm-Endposition ist
                actualArmEndY - originY,
                originX, originY, // Origin-Punkt entsprechend anpassen
                iconSize * scale, iconSize * scale,
                dir, 1f,
                itemRotation
            );
        }
    }

    public boolean facingLeft() {
        float scale = GameScreen.TILE_SIZE / 16f;
        float baseX = x * GameScreen.TILE_SIZE;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        float headSize    = 8 * scale;
        float headOffsetX = -1 * scale;
        float headOffsetY = 22 * scale;

        float headX = baseX + headOffsetX;
        float originX = headSize / 2f;
        float pivotX  = headX + originX;

        return mouse.x < pivotX;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }
}
