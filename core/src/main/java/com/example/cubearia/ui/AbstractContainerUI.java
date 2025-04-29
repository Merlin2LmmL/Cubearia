package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;
import java.util.List;

public abstract class AbstractContainerUI {
    private final Texture background;
    private final UILayout layout;
    private final RecipeRegistry recipeRegistry;
    private final int craftingGridSize;

    protected final ShapeRenderer shapeRenderer = new ShapeRenderer();
    protected final Vector3 touchPoint = new Vector3();
    protected List<SlotRegion> regions;
    protected float mouseX, mouseY;
    protected ItemStack draggedStack;
    protected SlotRegion originRegion;

    public AbstractContainerUI(Texture background,
                               UILayout layout,
                               RecipeRegistry recipeRegistry,
                               int craftingGridSize) {
        this.background = background;
        this.layout = layout;
        this.recipeRegistry = recipeRegistry;
        this.craftingGridSize = craftingGridSize;
    }

    public void render(SpriteBatch batch, OrthographicCamera cam) {
        updateMouse(Gdx.input.getX(), Gdx.input.getY(), cam);
        drawBackground(batch, cam);
        updateRegions(cam);
        if (recipeRegistry != null) updateCraftingOutput();
        drawStacks(batch);
        drawDragged(batch);
        if (Settings.DEBUG) debugRender(batch, cam);
    }

    public void handleTouchDown(int screenX, int screenY, OrthographicCamera cam, boolean leftClick) {
        updateMouse(screenX, screenY, cam);
        updateRegions(cam);
        SlotRegion clicked = findRegion(mouseX, mouseY);
        if (clicked == null) return;
        if (recipeRegistry != null && clicked.getType() == SlotRegion.Type.OUTPUT) {
            defaultOutputClick();
        } else {
            handleSlotClick(clicked, leftClick);
        }
    }

    public void handleTouchUp(int screenX, int screenY, OrthographicCamera cam) {
        // Optional
    }

    private void drawBackground(SpriteBatch batch, OrthographicCamera cam) {
        float[] b = computeBackground(cam.viewportWidth, cam.viewportHeight);
        batch.draw(background, b[0], b[1], b[2], b[3]);
    }

    private void updateRegions(OrthographicCamera cam) {
        float[] b = computeBackground(cam.viewportWidth, cam.viewportHeight);
        regions = layout.create(b[0], b[1], b[2], b[3]);
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
        return new float[]{(vw - w) / 2f, (vh - h) / 2f, w, h};
    }

    private void drawStacks(SpriteBatch batch) {
        for (SlotRegion r : regions) {
            ItemStack s = getStack(r);
            if (s != null && !s.isEmpty()) {
                Rectangle rect = r.getBounds();
                s.drawStack(batch, rect.x, rect.y, rect.width);
            }
        }
    }

    private void drawDragged(SpriteBatch batch) {
        if (draggedStack != null) {
            float size = regions.get(0).getBounds().width * 0.9f;
            draggedStack.drawStack(batch, mouseX - size/2f, mouseY - size/2f, size);
        }
    }

    private void debugRender(SpriteBatch batch, OrthographicCamera cam) {
        batch.end();
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for (SlotRegion r : regions) {
            Rectangle rect = r.getBounds();
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();
        batch.begin();
    }

    // --- Drag & Drop ---
    private void handleSlotClick(SlotRegion to, boolean leftClick) {
        ItemStack slot = getStack(to);
        if (draggedStack == null) {
            pickUp(to, slot, leftClick ? 1f : 0.5f);
        } else {
            placeOrMerge(to, slot, !leftClick);
        }
    }

    private void pickUp(SlotRegion from, ItemStack slot, float percent) {
        if (slot == null || slot.isEmpty()) return;
        int amt = Math.round(slot.getAmount() * percent);
        draggedStack = new ItemStack(slot.getItem(), amt);
        if (percent == 1) {
            setStack(from, null);
        } else {
            setStack(from, new ItemStack(slot.getItem(), slot.getAmount() - amt));
        }
        originRegion = from;
    }

    private void placeOrMerge(SlotRegion to, ItemStack slot, boolean onlyOne) {
        if (to.getType() == SlotRegion.Type.OUTPUT) return;
        if (draggedStack == null || draggedStack.isEmpty()) return;
        if (to.getType() == SlotRegion.Type.ARMOR && draggedStack.getItem().getType() != 8) return;

        if (slot == null || slot.isEmpty()) {
            if (onlyOne) {
                setStack(to, new ItemStack(draggedStack.getItem(), 1));
                if (draggedStack.getAmount() > 1) {
                    draggedStack.decrease(1);
                } else {
                    clearOrigin();
                }
            } else {
                setStack(to, draggedStack);
                clearOrigin();
            }
            return;
        }

        if (slot.getItem().equals(draggedStack.getItem())) {
            int max = slot.getItem().getMaxStackSize();
            if (onlyOne) {
                if (slot.getAmount() < max) {
                    slot.increase(1);
                    setStack(to, slot);
                    adjustDraggedAfterPlace();
                }
            } else {
                int space = max - slot.getAmount();
                if (space >= draggedStack.getAmount()) {
                    slot.increase(draggedStack.getAmount());
                    setStack(to, slot);
                    clearOrigin();
                } else {
                    slot.increase(space);
                    draggedStack.decrease(space);
                    setStack(to, slot);
                }
            }
            return;
        }

        ItemStack oldSlot = slot;
        ItemStack oldDragged = draggedStack;
        setStack(to, oldDragged);
        draggedStack = oldSlot;
        originRegion = to;
    }

    private void adjustDraggedAfterPlace() {
        if (draggedStack.getAmount() <= 1) clearOrigin();
        else draggedStack.setAmount(draggedStack.getAmount() - 1);
    }

    private void clearOrigin() { draggedStack = null; originRegion = null; }

    private void updateCraftingOutput() {
        String[][] grid = getGridSnapshot();
        ItemStack resultKey = recipeRegistry.matchRecipeWithAutoPadding(grid);
        SlotRegion out = findOutputRegion();
        if (resultKey != null) setStack(out, resultKey);
        else setStack(out, null);
    }

    private void defaultOutputClick() {
        String[][] grid = getGridSnapshot();
        ItemStack result = recipeRegistry.matchRecipeWithAutoPadding(grid);
        if (result == null) return;

        Item item = result.getItem();
        int resultAmt = result.getAmount();
        int maxStack = item.getMaxStackSize();

        if (draggedStack == null) {
            draggedStack = new ItemStack(item, resultAmt);

        } else if (draggedStack.getItem() == item) {
            int currentAmt = draggedStack.getAmount();
            if (currentAmt + resultAmt <= maxStack) {
                draggedStack = new ItemStack(item, currentAmt + resultAmt);
            } else {
                return;
            }

        } else {
            return;
        }

        originRegion = findOutputRegion();
        setStack(originRegion, null);

        for (SlotRegion r : regions) {
            if (r.getType() == SlotRegion.Type.CRAFTING2x2
                || r.getType() == SlotRegion.Type.CRAFTING3x3) {
                ItemStack s = getStack(r);
                if (s != null) {
                    if (s.getAmount() <= 1) {
                        setStack(r, null);
                    } else {
                        setStack(r, new ItemStack(s.getItem(), s.getAmount() - 1));
                    }
                }
            }
        }
    }


    private SlotRegion findOutputRegion() {
        return regions.stream().filter(r -> r.getType() == SlotRegion.Type.OUTPUT).findFirst().orElse(null);
    }

    private void updateMouse(int sx, int sy, OrthographicCamera cam) {
        touchPoint.set(sx, sy, 0); cam.unproject(touchPoint);
        mouseX = touchPoint.x; mouseY = touchPoint.y;
    }

    private SlotRegion findRegion(float x, float y) {
        return regions.stream().filter(r -> r.getBounds().contains(x,y)).findFirst().orElse(null);
    }

    protected abstract ItemStack getStack(SlotRegion region);
    protected abstract void setStack(SlotRegion region, ItemStack stack);
    protected abstract String[][] getGridSnapshot();
}
