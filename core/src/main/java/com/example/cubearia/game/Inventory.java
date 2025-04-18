package com.example.cubearia.game;

public class Inventory {
    private final ItemStack[] slots;
    private final int size;

    public Inventory(int size) {
        this.size = size;
        this.slots = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            slots[i] = null; // leere Slots
        }
    }

    /**
     * Versucht, `amount` Items in das Inventar zu packen.
     * @return Anzahl der Items, die nicht mehr untergebracht werden konnten.
     */
    public void addItem(Item item, int amount) {
        // 1) Erst vorhandene Stacks befüllen
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i];
            if (stack != null && stack.getItem().equals(item) && stack.getAmount() < item.getMaxStackSize()) {
                int space = item.getMaxStackSize() - stack.getAmount();
                int toAdd = Math.min(space, amount);
                stack.increase(toAdd);
                amount -= toAdd;
            }
        }

        // 2) Dann neue Stacks anlegen (jeweils bis maxStackSize)
        for (int i = 0; i < slots.length && amount > 0; i++) {
            if (slots[i] == null) {
                int toAdd = Math.min(item.getMaxStackSize(), amount);
                slots[i] = new ItemStack(item, toAdd);
                amount -= toAdd;
            }
        }
    }

    public ItemStack getSlot(int index) {
        return (index >= 0 && index < size) ? slots[index] : null;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < size) {
            slots[index] = null;
        }
    }

    public int getSize() {
        return size;
    }
}
