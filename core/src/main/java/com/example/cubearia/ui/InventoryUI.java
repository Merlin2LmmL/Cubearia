package com.example.cubearia.ui;

import com.badlogic.gdx.graphics.Texture;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;

class InventoryUI extends AbstractContainerUI {
    private final com.example.cubearia.game.inventory.Inventory inventory;
    private final RecipeRegistry recipeRegistry;
    private static final int CRAFTING_SIZE = 2;

    public InventoryUI(com.example.cubearia.game.inventory.Inventory inventory, RecipeRegistry recipeRegistry) {
        super(Assets.inventoryBackground, UILayout.INVENTORY, recipeRegistry, CRAFTING_SIZE);
        this.inventory = inventory;
        this.recipeRegistry = recipeRegistry;
    }

    @Override
    protected ItemStack getStack(SlotRegion region) {
        switch (region.getType()) {
            case INVENTORY: case HOTBAR: return inventory.getSlot(region.getIndex());
            case ARMOR:          return inventory.getArmorSlot(region.getIndex()-100);
            case CRAFTING2x2:    return inventory.getCraftingInput(region.getIndex()-200);
            case OUTPUT:         return inventory.getCraftingOutput();
            default:             return null;
        }
    }

    @Override
    protected void setStack(SlotRegion region, ItemStack stack) {
        if (region==null) return;
        switch (region.getType()) {
            case INVENTORY: case HOTBAR: inventory.setSlot(region.getIndex(),stack); break;
            case ARMOR:          inventory.setArmorSlot(region.getIndex()-100,stack); break;
            case CRAFTING2x2:    inventory.setCraftingInput(region.getIndex()-200,stack); break;
            case OUTPUT:         inventory.setCraftingOutput(stack); break;
        }
    }

    @Override
    protected String[][] getGridSnapshot() {
        String[][] grid = new String[CRAFTING_SIZE][CRAFTING_SIZE];
        for (int i=0; i<CRAFTING_SIZE*CRAFTING_SIZE; i++) {
            ItemStack s = inventory.getCraftingInput(i);
            grid[i/CRAFTING_SIZE][i%CRAFTING_SIZE] = (s==null?null:s.getItem().getName());
        }
        return grid;
    }
}
