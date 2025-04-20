package com.example.cubearia.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    public float x;
    public float y;
    public float width;
    public float height;
    protected boolean toRemove = false;

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
}
