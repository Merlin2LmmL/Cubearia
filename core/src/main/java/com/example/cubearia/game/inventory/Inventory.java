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

    /**
     * Liest den ItemStack an der gegebenen Position.
     * @param index Slot-Index
     * @return der ItemStack oder null, wenn leer/ungültig
     */
    public ItemStack getSlot(int index) {
        return (index >= 0 && index < slots.length) ? slots[index] : null;
    }

    /**
     * Setzt den ItemStack an die gegebene Position (kann auch auf null gesetzt werden).
     * @param index Slot-Index
     * @param stack Neuer ItemStack oder null
     */
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < slots.length) {
            slots[index] = stack;
        }
    }

    /**
     * Tauscht die beiden Slots indexA und indexB miteinander.
     * @param indexA erster Slot
     * @param indexB zweiter Slot
     */
    public void swapSlots(int indexA, int indexB) {
        if (indexA >= 0 && indexA < slots.length && indexB >= 0 && indexB < slots.length) {
            ItemStack tmp = slots[indexA];
            slots[indexA] = slots[indexB];
            slots[indexB] = tmp;
        }
    }

    /**
     * Entfernt den Stack an der Position (setzt ihn auf null).
     * @param index Slot-Index
     */
    public void removeItem(int index) {
        if (index >= 0 && index < slots.length) {
            slots[index] = null;
        }
    }

    /** Anzahl der Spalten im Inventar. */
    public int getColumns() {
        return columns;
    }

    /** Anzahl der Reihen im Inventar. */
    public int getRows() {
        return rows;
    }

    /** Gesamtanzahl der Slots. */
    public int getTotalSlots() {
        return slots.length;
    }
}
