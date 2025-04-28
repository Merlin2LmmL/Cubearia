package com.example.cubearia.game.inventory;

import com.example.cubearia.game.inventory.DroppedItem;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.world.World;

public class Inventory {
    private final ItemStack[] slots;
    private final ItemStack[] armorSlots;         // neu: Rüstungsslots
    private final ItemStack[] craftingInputs;      // neu: 2x2 Crafting-Input
    private ItemStack craftingOutput;
    private final int columns;
    private final int rows;

    public Inventory(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.slots = new ItemStack[columns * rows];
        this.armorSlots = new ItemStack[4];           // Kopf, Brust, Hose, Schuhe
        this.craftingInputs = new ItemStack[9];        // 2x2 Grid
    }
    public ItemStack getSlot(int index) {
        return (index >= 0 && index < slots.length) ? slots[index] : null;
    }

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

    /** Rüstungs-Slot lesen/setzen */
    public ItemStack getArmorSlot(int index) {
        if (index >= 0 && index < armorSlots.length) {
            return armorSlots[index];
        }
        return null;
    }
    public void setArmorSlot(int index, ItemStack stack) {
        if (index >= 0 && index < armorSlots.length) {
            armorSlots[index] = stack;
        }
    }

    /** Crafting-Input lesen/setzen */
    public ItemStack getCraftingInput(int index) {
        if (index >= 0 && index < craftingInputs.length) {
            return craftingInputs[index];
        }
        return null;
    }
    public void setCraftingInput(int index, ItemStack stack) {
        if (index >= 0 && index < craftingInputs.length) {
            craftingInputs[index] = stack;
        }
    }

    /** Beim Schließen: Crafting-Inhalte droppen */
    public void dropCraftingAndClear(Player player, World world) {
        // CraftingInputs
        for (int i = 0; i < craftingInputs.length; i++) {
            ItemStack stack = craftingInputs[i];
            if (stack != null && !stack.isEmpty()) {
                // Droppe gesamte Menge direkt
                DroppedItem item = new DroppedItem (
                    world,
                    player.x, player.y,
                    stack.getItem(),
                    0f, 0f,
                    stack.getAmount()
                );
                world.addEntity(item);
                // Slot leeren
                craftingInputs[i] = null;
            }
        }
    }

    public ItemStack getCraftingOutput() {
        return craftingOutput;
    }

    public void setCraftingOutput(ItemStack stack) {
        craftingOutput = stack;
    }

    public boolean hasSpace() {
        for (int i=0; i<slots.length; i++) {
            if (slots[i] == null || slots[i].getAmount() < slots[i].getItem().getMaxStackSize()) return true;
        }
        return false;
    }
}
