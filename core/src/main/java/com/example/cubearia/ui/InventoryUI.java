package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;

import java.util.ArrayList;
import java.util.List;

public class InventoryUI {
    private final Inventory inventory;
    private final RecipeRegistry recipeRegistry;
    private final Texture background;

    private ItemStack draggedStack;
    private SlotRegion originRegion;
    private final Vector3 touchPoint = new Vector3();
    private final List<SlotRegion> regions = new ArrayList<>();
    private float mouseX, mouseY;

    public InventoryUI(Inventory inventory, RecipeRegistry recipeRegistry) {
        this.inventory = inventory;
        this.recipeRegistry = recipeRegistry;
        this.background = Assets.inventoryBackground;
        updateCraftingOutput();
    }

    public void render(SpriteBatch batch, OrthographicCamera cam) {
        // Mausposition & Layout updaten
        updateMouse(Gdx.input.getX(), Gdx.input.getY(), cam);
        drawBackground(batch, cam);
        updateRegions(cam);

        // Crafting-Ausgabe bei jeder Render-Aufruf aktualisieren
        updateCraftingOutput();

        // Stacks & Drag-Icon zeichnen
        drawStacks(batch);
        drawDragged(batch);
    }

    public void handleTouchDown(int screenX, int screenY, OrthographicCamera cam, boolean leftClick) {
        updateMouse(screenX, screenY, cam);
        updateRegions(cam);
        SlotRegion clicked = findRegion(mouseX, mouseY);
        if (clicked == null) return;

        if (clicked.getType() == SlotRegion.Type.OUTPUT) {
            handleOutputClick(clicked);
        } else {
            handleSlotClick(clicked, leftClick);
        }
        updateCraftingOutput();
    }

    public void handleTouchUp(int screenX, int screenY, OrthographicCamera cam) {
        // Optional: snap-back or drop-outside
    }

    private void drawBackground(SpriteBatch batch, OrthographicCamera cam) {
        float[] b = computeBackground(cam.viewportWidth, cam.viewportHeight);
        batch.draw(background, b[0], b[1], b[2], b[3]);
    }

    private void drawStacks(SpriteBatch batch) {
        for (SlotRegion r : regions) {
            ItemStack stack = getStack(r);
            if (stack != null && !stack.isEmpty()) {
                Rectangle rect = r.getBounds();
                stack.drawStack(batch, rect.x, rect.y, rect.width);
            }
        }
    }

    private void drawDragged(SpriteBatch batch) {
        if (draggedStack != null) {
            float size = regions.get(0).getBounds().width * 0.9f;
            draggedStack.drawStack(batch, mouseX - size / 2f, mouseY - size / 2f, size);
        }
    }

    private void handleOutputClick(SlotRegion outputRegion) {
        String[][] grid = getGridSnapshot();
        String resultKey = recipeRegistry.matchRecipe(grid);
        if (resultKey == null) return;

        Item resultItem = Item.valueOf(recipeRegistry.matchRecipe(getGridSnapshot()));

        ItemStack out = new ItemStack(resultItem, recipeRegistry.matchRecipeAmount(grid));
        inventory.setCraftingOutput(out);

        int size = getCraftingGridSize();
        for (int i = 0; i < size * size; i++) {
            if (inventory.getCraftingInput(i) != null) {
                if (inventory.getCraftingInput(i).getAmount() <= 1) {
                    inventory.setCraftingInput(i, null);
                } else {
                    inventory.setCraftingInput(i, new ItemStack(inventory.getCraftingInput(i).getItem(), inventory.getCraftingInput(i).getAmount() - 1));
                }
            }
        }
        if (draggedStack == null) {
            draggedStack = new ItemStack(out.getItem(), out.getAmount());
        } else {
            draggedStack = new ItemStack(out.getItem(), draggedStack.getAmount() + out.getAmount());
        }
        originRegion = outputRegion;
        inventory.setCraftingOutput(null);
        setStack(outputRegion, null);
    }

    private void handleSlotClick(SlotRegion clicked, boolean leftClick) {
        ItemStack slotStack = getStack(clicked);
        if (draggedStack == null) {
            if (leftClick) {
                pickUpStack(clicked, slotStack, 1f);
            } else {
                pickUpStack(clicked, slotStack, 0.5f);
            }
        } else {
            if (leftClick) {
                placeOrMergeStack(clicked, slotStack, false);
            } else {
                placeOrMergeStack(clicked, slotStack, true);
            }
        }
    }

    private void pickUpStack(SlotRegion from, ItemStack slotStack, float percantage) {
        if (slotStack == null || slotStack.isEmpty()) return;
        int pickUpAmount = Math.round(slotStack.getAmount() * percantage);
        draggedStack = new ItemStack(slotStack.getItem(), pickUpAmount);
        setStack(from, new ItemStack(slotStack.getItem(), slotStack.getAmount() - pickUpAmount));
    }

    private void placeOrMergeStack(SlotRegion to, ItemStack slotStack, boolean onlyOne) {
        if (to.getType() == SlotRegion.Type.OUTPUT) return;

        if (!onlyOne) {
            if (slotStack == null || slotStack.isEmpty()) {
                setStack(to, draggedStack);
                clearOrigin();
            } else if (slotStack.getItem().equals(draggedStack.getItem())) {
                int max = slotStack.getItem().getMaxStackSize();
                int total = slotStack.getAmount() + draggedStack.getAmount();
                if (total <= max) {
                    slotStack.setAmount(total);
                    setStack(to, slotStack);
                    clearOrigin();
                } else {
                    slotStack.setAmount(max);
                    draggedStack.setAmount(total - max);
                    setStack(to, slotStack);
                    setStack(originRegion, draggedStack);
                    originRegion = to;
                }
            }
        } else {
            if (slotStack == null || slotStack.isEmpty()) {
                setStack(to, new ItemStack(draggedStack.getItem(), 1));
                if (draggedStack.getAmount() <= 1) {
                    draggedStack = null;
                } else {
                    draggedStack = new ItemStack(draggedStack.getItem(), draggedStack.getAmount() - 1);
                }
            } else if (slotStack.getItem().equals(draggedStack.getItem())) {
                int max = slotStack.getItem().getMaxStackSize();
                int total = slotStack.getAmount() + 1;
                if (total <= max) {
                    slotStack.setAmount(total);
                    setStack(to, slotStack);
                    if (draggedStack.getAmount() <= 1) {
                        draggedStack = null;
                    } else {
                        draggedStack = new ItemStack(draggedStack.getItem(), draggedStack.getAmount() - 1);
                    }
                }
            }
        }
    }

    private void updateMouse(float sx, float sy, OrthographicCamera cam) {
        touchPoint.set(sx, sy, 0);
        cam.unproject(touchPoint);
        mouseX = touchPoint.x;
        mouseY = touchPoint.y;
    }

    private void updateRegions(OrthographicCamera cam) {
        float[] b = computeBackground(cam.viewportWidth, cam.viewportHeight);
        regions.clear();
        regions.addAll(UILayout.INVENTORY.create(b[0], b[1], b[2], b[3]));
    }

    private SlotRegion findRegion(float x, float y) {
        return regions.stream()
            .filter(r -> r.getBounds().contains(x, y))
            .findFirst()
            .orElse(null);
    }

    private void updateCraftingOutput() {
        String[][] grid = getGridSnapshot();
        String resultKey = recipeRegistry.matchRecipe(grid);
        if (resultKey != null) {
            inventory.setCraftingOutput(new ItemStack(Item.valueOf(resultKey), recipeRegistry.matchRecipeAmount(grid)));
        } else {
            inventory.setCraftingOutput(null);
        }
    }

    private String[][] getGridSnapshot() {
        int size = getCraftingGridSize();
        String[][] grid = new String[size][size];
        for (int i = 0; i < size * size; i++) {
            ItemStack s = inventory.getCraftingInput(i);
            grid[i / size][i % size] = (s == null) ? null : s.getItem().getName();
        }
        return grid;
    }

    private ItemStack getStack(SlotRegion r) {
        switch (r.getType()) {
            case INVENTORY:
            case HOTBAR:
                return inventory.getSlot(r.getIndex());
            case ARMOR:
                return inventory.getArmorSlot(r.getIndex() - 100);
            case CRAFTING2x2:
                return inventory.getCraftingInput(r.getIndex() - 200);
            case OUTPUT:
                return inventory.getCraftingOutput();
            default:
                return null;
        }
    }

    private void setStack(SlotRegion r, ItemStack s) {
        if (r != null) {
            switch (r.getType()) {
                case INVENTORY:
                case HOTBAR:
                    inventory.setSlot(r.getIndex(), s);
                    break;
                case ARMOR:
                    inventory.setArmorSlot(r.getIndex() - 100, s);
                    break;
                case CRAFTING2x2:
                    inventory.setCraftingInput(r.getIndex() - 200, s);
                    break;
                case OUTPUT:
                    inventory.setCraftingOutput(s);
                    break;
            }
        }
    }

    private void clearOrigin() {
        draggedStack = null;
        originRegion = null;
    }

    private float[] computeBackground(float vw, float vh) {
        float scale = Settings.UISCALE;
        float tw = background.getWidth(), th = background.getHeight();
        float ratio = tw / th;
        float w = vw * scale;
        float h = w / ratio;
        if (h > vh * scale * 0.95f) {
            h = vh * scale * 0.95f;
            w = h * ratio;
        }
        return new float[]{ (vw - w) / 2f, (vh - h) / 2f, w, h };
    }

    private int getCraftingGridSize() {
        return 2; // TODO: dynamisch anpassbar
    }
}
