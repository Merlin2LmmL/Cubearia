package com.example.cubearia.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.Settings;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

class InventoryUI {
    private final Inventory inventory;
    private final Texture background;

    private ItemStack draggedStack = null;
    private int draggedIndex = -1;

    private float mouseX, mouseY;
    private final Vector3 mouseVec = new Vector3();

    private List<SlotRegion> regions = new ArrayList<>();

    public InventoryUI(Inventory inventory) {
        this.inventory = inventory;
        this.background = Assets.inventoryBackground;
    }

    public void updateMousePosition(float screenX, float screenY, OrthographicCamera uiCam) {
        mouseVec.set(screenX, screenY, 0);
        uiCam.unproject(mouseVec);
        mouseX = mouseVec.x;
        mouseY = mouseVec.y;
    }

    public void handleTouchDown(float screenX, float screenY, OrthographicCamera uiCam) {
        float vw = uiCam.viewportWidth, vh = uiCam.viewportHeight;
        float[] bgBox = computeBackgroundBox(vw, vh);
        updateRegions(bgBox);

        updateMousePosition(screenX, screenY, uiCam);

        SlotRegion region = findRegionAt(mouseX, mouseY);
        if (region == null) return;

        int idx = region.getIndex();
        ItemStack stack = inventory.getSlot(idx);
        if (stack != null && !stack.isEmpty()) {
            draggedStack = stack;
            draggedIndex = idx;
            inventory.setSlot(idx, null);
        }
    }

    public void handleTouchUp(float screenX, float screenY, OrthographicCamera uiCam) {
        if (draggedStack == null) return;

        float vw = uiCam.viewportWidth, vh = uiCam.viewportHeight;
        float[] bgBox = computeBackgroundBox(vw, vh);
        updateRegions(bgBox);
        updateMousePosition(screenX, screenY, uiCam);

        SlotRegion region = findRegionAt(mouseX, mouseY);
        if (region != null) {
            int targetIdx = region.getIndex();
            if (targetIdx == draggedIndex) {
                inventory.setSlot(draggedIndex, draggedStack);
            } else {
                ItemStack targetStack = inventory.getSlot(targetIdx);
                inventory.setSlot(targetIdx, draggedStack);
                inventory.setSlot(draggedIndex, targetStack);
            }
        } else {
            inventory.setSlot(draggedIndex, draggedStack);
        }

        draggedStack = null;
        draggedIndex = -1;
    }

    public void render(SpriteBatch batch, OrthographicCamera uiCam) {
        float vw = uiCam.viewportWidth, vh = uiCam.viewportHeight;
        float[] bgBox = computeBackgroundBox(vw, vh);
        float bgX = bgBox[0], bgY = bgBox[1], bgW = bgBox[2], bgH = bgBox[3];

        // Hintergrund zeichnen
        batch.draw(background, bgX, bgY, bgW, bgH);

        // Slot-Bereiche aktualisieren
        updateRegions(bgBox);

        // Slots zeichnen
        for (SlotRegion region : regions) {
            Rectangle b = region.getBounds();
            ItemStack stack = inventory.getSlot(region.getIndex());
            if (stack != null && !stack.isEmpty()) {
                stack.drawStack(batch, b.x, b.y, b.width);
            }
        }

        // Gezogener Stack
        if (draggedStack != null && !regions.isEmpty()) {
            float slotSize = regions.get(0).getWidth();
            float ds = slotSize * 0.9f;
            draggedStack.drawStack(batch, mouseX - ds / 2f, mouseY - ds / 2f, ds);
        }
    }

    private void updateRegions(float[] bgBox) {
        float bgX = bgBox[0], bgY = bgBox[1], bgW = bgBox[2], bgH = bgBox[3];
        List<SlotRegion> base = UILayout.INVENTORY.create(bgX, bgY, bgW, bgH);
        regions.clear();
        regions.addAll(base);
    }

    private SlotRegion findRegionAt(float x, float y) {
        for (SlotRegion region : regions) {
            if (region.getBounds().contains(x, y)) {
                return region;
            }
        }
        return null;
    }

    private float[] computeBackgroundBox(float vw, float vh) {
        float scale = Settings.UISCALE;
        float texW = background.getWidth(), texH = background.getHeight();
        float ratio = texW / texH;
        float maxW = vw * scale, maxH = vh * scale * 0.95f;
        float bgW = maxW, bgH = bgW / ratio;
        if (bgH > maxH) {
            bgH = maxH;
            bgW = bgH * ratio;
        }
        float bgX = (vw - bgW) / 2f, bgY = (vh - bgH) / 2f;
        return new float[]{bgX, bgY, bgW, bgH};
    }
}
