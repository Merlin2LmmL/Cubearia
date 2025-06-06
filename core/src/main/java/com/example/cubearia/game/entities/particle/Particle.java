package com.example.cubearia.game.entities.particle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.GameScreen;

public class Particle {
    public static final int gravity = 7;
    public float x, y;
    public float vx, vy;
    public float life = 0f;
    public float maxLife;
    public TextureRegion texture;

    public Particle(float x, float y, float vx, float vy, float maxLife, TextureRegion texture) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.maxLife = maxLife;
        this.texture = texture;
    }

    public void update(float delta) {
        life += delta;
        vy -= gravity * delta;
        x += vx * delta;
        y += vy * delta;
    }

    public void render(SpriteBatch batch) {
        if (life < maxLife) {
            float size = GameScreen.TILE_SIZE / 8f; // kleiner Pixel
            batch.draw(texture, x * GameScreen.TILE_SIZE, y * GameScreen.TILE_SIZE, size, size);
        }
    }

    public boolean isDead() {
        return life >= maxLife;
    }
}
