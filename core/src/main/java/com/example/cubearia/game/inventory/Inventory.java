package com.example.cubearia.game.inventory;

public class Inventory {
    private final ItemStack[] slots;
    private final int columns;
    private final int rows;

    public Inventory(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.slots = new ItemStack[columns * rows];
    }

    /**
     * Versucht, `amount` Items vom Typ `item` einzufügen.
     * @param item   der Item-Typ
     * @param amount Anzahl der einzufügenden Items
     * @return 0, wenn alle Items eingefügt wurden; sonst Anzahl der Items, die nicht untergekommen sind
     */
    public int addItem(Item item, int amount) {
        // 1) Erst vorhandene Stacks befüllen
        for (int i = 0; i < slots.length && amount > 0; i++) {
            ItemStack stack = slots[i];
            if (stack != null
                    && stack.getItem().equals(item)
                    && stack.getAmount() < item.getMaxStackSize()) {
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

        // Menge, die nicht eingefügt werden konnte
        return amount;
    }

    public ItemStack getSlot(int index) {
        return (index >= 0 && index < slots.length) ? slots[index] : null;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < slots.length) {
            slots[index] = null;
        }
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getTotalSlots() {
        return slots.length;
    }
}
