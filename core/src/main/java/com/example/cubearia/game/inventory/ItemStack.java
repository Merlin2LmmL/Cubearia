package com.example.cubearia.game.inventory;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ItemStack {
    private final Item item;
    private int amount;
    private final BitmapFont font;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = amount;
        this.font = new BitmapFont();
    }

    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void increase(int value) {
        amount += value;
    }

    public void decrease(int value) {
        amount -= value;
        if (amount < 0) amount = 0;
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean canStackWith(Item other) {
        return item.isStackable() && item.equals(other);
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void drawStack(SpriteBatch batch, float x, float y, float size) {
        float iconSize = size * 0.7f;
        float offset = size * 0.1f;
        batch.draw(getItem().getIcon(), x + offset, y + offset, iconSize, iconSize);
        font.setUseIntegerPositions(false);
        font.getData().setScale(size * 0.025f);
        font.draw(batch, String.valueOf(getAmount()), x + size*0.5f, y + size*0.25f);
    }
}
