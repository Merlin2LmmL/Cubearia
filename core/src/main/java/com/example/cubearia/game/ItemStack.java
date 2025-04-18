package com.example.cubearia.game;

public class ItemStack {
    private final Item item;
    private int amount;

    public ItemStack(Item item, int amount) {
        this.item = item;
        this.amount = amount;
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
}
