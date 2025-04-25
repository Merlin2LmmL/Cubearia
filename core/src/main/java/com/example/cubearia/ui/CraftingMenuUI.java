package com.example.cubearia.ui;

import com.badlogic.gdx.graphics.Texture;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;

/**
 * UI-Implementation für das 3×3-Crafting-Menü.
 */
public class CraftingMenuUI extends AbstractContainerUI {
    private final Inventory inventory;
    private static final int CRAFTING_SIZE = 3;
    private static final int INDEX_OFFSET = 200;

    public CraftingMenuUI(Inventory inventory, RecipeRegistry recipeRegistry) {
        super(Assets.craftingTableBackground, UILayout.CRAFTING_TABLE, recipeRegistry, CRAFTING_SIZE);
        this.inventory = inventory;
    }

    @Override
    protected ItemStack getStack(SlotRegion region) {
        switch (region.getType()) {
            case INVENTORY:
            case HOTBAR:
                return inventory.getSlot(region.getIndex());
            case ARMOR:
                return inventory.getArmorSlot(region.getIndex() - 100);
            case CRAFTING3x3:
                int readIdx = region.getIndex() - INDEX_OFFSET;
                return inventory.getCraftingInput(readIdx);
            case OUTPUT:
                return inventory.getCraftingOutput();
            default:
                return null;
        }
    }

    @Override
    protected void setStack(SlotRegion region, ItemStack stack) {
        if (region == null) return;
        // Leere Stacks auf null setzen
        if (stack != null && stack.getAmount() <= 0) stack = null;

        switch (region.getType()) {
            case INVENTORY:
            case HOTBAR:
                inventory.setSlot(region.getIndex(), stack);
                break;
            case ARMOR:
                inventory.setArmorSlot(region.getIndex() - 100, stack);
                break;
            case CRAFTING3x3:
                int writeIdx = region.getIndex() - INDEX_OFFSET;
                if (writeIdx >= 0 && writeIdx < CRAFTING_SIZE * CRAFTING_SIZE) {
                    inventory.setCraftingInput(writeIdx, stack);
                }
                break;
            case OUTPUT:
                inventory.setCraftingOutput(stack);
                break;
        }
    }

    @Override
    protected String[][] getGridSnapshot() {
        String[][] grid = new String[CRAFTING_SIZE][CRAFTING_SIZE];
        for (int i = 0; i < CRAFTING_SIZE * CRAFTING_SIZE; i++) {
            ItemStack s = inventory.getCraftingInput(i);
            grid[i / CRAFTING_SIZE][i % CRAFTING_SIZE] = (s == null ? null : s.getItem().getName());
        }
        return grid;
    }
}
