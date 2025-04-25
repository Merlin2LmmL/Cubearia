package com.example.cubearia.ui;

import com.badlogic.gdx.math.Rectangle;

public class SlotRegion {
    private final int index;
    private final float x, y;
    private final float width, height;
    private final Type type;

    public enum Type {
        INVENTORY,
        HOTBAR,
        ARMOR,
        CRAFTING2x2,
        CRAFTING3x3,
        OUTPUT,
        INPUT,
        FUEL
    }

    public SlotRegion(int index, float x, float y, float width, float height, Type type) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Type getType() {
        return type;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean contains(float touchX, float touchY) {
        return getBounds().contains(touchX, touchY);
    }

    @Override
    public String toString() {
        return "SlotRegion{" +
            "index=" + index +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            ", type=" + type +
            '}';
    }
}
