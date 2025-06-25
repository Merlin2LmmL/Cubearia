package com.example.cubearia.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.example.cubearia.game.world.World;

public abstract class Entity {
    public float x;
    public float y;
    public float width;
    public float height;
    protected boolean toRemove = false;

    // Render distance tracking
    protected float timeOutsideRenderDistance = 0f;
    protected boolean isOutsideRenderDistance = false;
    private static final float MAX_TIME_OUTSIDE_RENDER_DISTANCE = 30f; // 30 seconds

    public Entity(float x, float y, float width, float height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    /** Logik-Update pro Frame */
    public abstract void update(float delta);

    /** Zeichnen mit dem übergebenen SpriteBatch */
    public abstract void render(SpriteBatch batch);

    /** Kollisionsrechteck erzeugen */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /** Markiere Entity zum Entfernen (z.B. wenn Tod) */
    public void remove() {
        toRemove = true;
    }

    public boolean isRemoved() {
        return toRemove;
    }

    /** Setzt die Position der Entity */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates render distance tracking for this entity
     * Should be called from the main update method
     */
    public void updateRenderDistanceTracking(float delta, java.util.List<Player> players) {
        // Skip this check for Player entities
        if (this instanceof Player) {
            return;
        }

        // Check if entity is within render distance of ANY player
        boolean withinRenderDistance = false;
        float renderDistanceSquared = com.example.cubearia.game.Settings.RENDERDISTANCE * com.example.cubearia.game.Settings.RENDERDISTANCE;

        for (Player player : players) {
            float dx = x - player.getX();
            float dy = y - player.getY();
            float distanceSquared = dx * dx + dy * dy;

            if (distanceSquared <= renderDistanceSquared) {
                withinRenderDistance = true;
                break;
            }
        }

        if (!withinRenderDistance) {
            // Entity is outside render distance of ALL players
            if (!isOutsideRenderDistance) {
                isOutsideRenderDistance = true;
                timeOutsideRenderDistance = 0f;
            }
            timeOutsideRenderDistance += delta;

            // Remove entity if it's been outside render distance for too long
            if (timeOutsideRenderDistance >= MAX_TIME_OUTSIDE_RENDER_DISTANCE) {
                remove();
            }
        } else {
            // Entity is within render distance of at least one player
            isOutsideRenderDistance = false;
            timeOutsideRenderDistance = 0f;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * Zentrale Fluid-Erkennung für alle Entities
     * Prüft, ob die Entity in einem Fluid ist (Layer 1)
     * Verwendet korrekte Block-Koordinaten ohne Versetzung
     */
    public boolean isInFluid(World world) {
        // Verwende die Mitte der Entity für präzise Fluid-Erkennung
        int blockX = (int) Math.floor(x + width / 2f);
        int blockY = (int) Math.floor(y);
        return world.getBlock(blockX, blockY, 1).isFluid();
    }
}
