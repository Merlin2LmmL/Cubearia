package com.example.cubearia.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.example.cubearia.game.GameScreen;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.world.BlockType;
import com.example.cubearia.game.world.World;

public class DroppedItem extends Entity {
    private final World world;      // referenz auf die Welt
    private final Item item;
    private int quantity;
    private float lifetime = 0f;
    private static final float MAX_LIFETIME = 60f; // nach 60 s verschwindet das Item
    private float velocityX = 0f;
    private float velocityY = 0f;
    public final float gravity = -10f;
    public boolean onGround = false;
    public final float friction = 100f;
    private boolean inFluid = false;

    public DroppedItem(World world, float x, float y, Item item, float velocityX, float velocityY, int quantity) {
        super(x, y, 0.5f, 0.5f);
        this.world    = world;
        this.item     = item;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.quantity = quantity;
    }

    @Override
    public void update(float delta) {
        lifetime += delta;

        checkFluidState();

        if (inFluid) {
            velocityY += gravity * 0.2f * delta; // weniger Gravitation im Wasser
            if (world.getBlock((int) x, (Math.round(y + 0.25f)), 1) == BlockType.WATER) {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f); // nicht zu schnell fallen
            } else {
                velocityY = MathUtils.clamp(velocityY, -3f, 3f); // nicht zu schnell fallen
            }
        } else {
            velocityY += gravity * delta;
        }

        applyFriction(delta);
        moveWithCollision(delta);
        if (lifetime >= MAX_LIFETIME) {
            remove();
            return;
        }

        // Aufsammel‑Logik: Spieler-Entity in Reichweite?
        for (Entity e : world.getEntities()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                float dx = (p.x + p.width/2) - (x + width/2);
                float dy = (p.y + p.height/2) - (y + height/2);
                if (dx*dx + dy*dy < 1.2f && p.getInventory().hasSpace()) {
                    int leftovers = p.getInventory().addItem(item, quantity);
                    quantity = leftovers;
                    break;
                }
            }
        }
        if (quantity <= 0) remove();
    }

    @Override
    public void render(SpriteBatch batch) {
        float sizePX = GameScreen.TILE_SIZE * 0.5f;
        float offset = (GameScreen.TILE_SIZE - sizePX) / 2f; // zentrieren

        float hoverOffset = (float)Math.sin(lifetime * 2f) * 0.1f + 0.1f; // 2f = Speed, 0.1f = Höhe

        batch.draw(
            item.getIcon(),
            x * GameScreen.TILE_SIZE + offset,
            (y + hoverOffset) * GameScreen.TILE_SIZE,
            sizePX, sizePX
        );
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

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

    private void checkFluidState() {
        inFluid = isInFluid(world);
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
}
