package com.example.cubearia.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.crafting.DefaultRecipes;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;
import com.example.cubearia.game.world.World;

/**
 * Verwalter für alle UI-Komponenten und Input-Routing.
 */
public class UIManager {
    private final Player player;
    private final OrthographicCamera uiCamera;
    private final World world;
    private final Inventory inventory;
    private final RecipeRegistry recipeRegistry;
    private final InventoryUI inventoryUI;
    private final CraftingMenuUI craftingMenuUI;
    private final HotbarUI hotbarUI;

    private boolean inventoryOpen = false;
    private boolean craftingTableOpen = false;
    private boolean inMenu = false;

    /**
     * Konstruktor initialisiert Rezepte und alle UI-Komponenten.
     */
    public UIManager(Player player,
                     OrthographicCamera uiCamera,
                     World world,
                     Inventory inventory) {
        this.player = player;
        this.uiCamera = uiCamera;
        this.world = world;
        this.inventory = inventory;

        this.recipeRegistry = new RecipeRegistry();
        DefaultRecipes.registerAll(this.recipeRegistry);

        this.inventoryUI    = new InventoryUI(inventory, recipeRegistry);
        this.craftingMenuUI = new CraftingMenuUI(inventory, recipeRegistry);
        this.hotbarUI       = new HotbarUI(inventory);
    }

    /**
     * Routen des Maus-Down-Events an die aktive Container-UI.
     */
    public void pressClick(int screenX, int screenY, int button) {
        routeTouchDown(screenX, screenY, button);
    }

    /**
     * Routen des Maus-Up-Events an die aktive Container-UI.
     */
    public void releaseClick(int screenX, int screenY, int button) {
        routeTouchUp(screenX, screenY, button);
    }

    /**
     * Helper für TouchDown-Routing.
     */
    private void routeTouchDown(int screenX, int screenY, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            boolean left = (button == Input.Buttons.LEFT);
            AbstractContainerUI active = inventoryOpen ? inventoryUI
                : craftingTableOpen ? craftingMenuUI
                : null;
            if (active != null) {
                active.handleTouchDown(screenX, screenY, uiCamera, left);
                player.inMenu = true;
            }
        }
    }

    /**
     * Helper für TouchUp-Routing.
     */
    private void routeTouchUp(int screenX, int screenY, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            AbstractContainerUI active = inventoryOpen ? inventoryUI
                : craftingTableOpen ? craftingMenuUI
                : null;
            if (active != null) {
                active.handleTouchUp(screenX, screenY, uiCamera);
                player.inMenu = true;
            }
        }
    }

    /**
     * Toggle des Inventar-Bildschirms.
     */
    public void toggleInventory() {
        if (!inventoryOpen && player.inMenu) {
            craftingTableOpen = !craftingTableOpen;
            player.inMenu = false;
            return;
        };
        inventoryOpen = !inventoryOpen;
        craftingTableOpen = false;
        player.inMenu = inventoryOpen;
        if (!inventoryOpen) {
            inventory.dropCraftingAndClear(player, world);
        }
    }

    /**
     * Toggle des Crafting-Tisch-Bildschirms.
     */
    public void toggleCraftingTable() {
        craftingTableOpen = !craftingTableOpen;
        inventoryOpen = false;
        player.inMenu = craftingTableOpen;
        if (!craftingTableOpen) {
            inventory.dropCraftingAndClear(player, world);
        }
    }

    /**
     * Render-Routinen für die aktive UI (Inventar, Crafting oder Hotbar).
     */
    public void render(SpriteBatch batch) {
        if (inventoryOpen) {
            inventoryUI.render(batch, uiCamera);
        } else if (craftingTableOpen) {
            craftingMenuUI.render(batch, uiCamera);
        } else {
            hotbarUI.render(batch, uiCamera, player);
        }
    }

    /**
     * Optional: Weiterleitung für Maus-Position (z.B. Tooltip-Follow).
     */
    public void updateMousePosition(float screenX, float screenY) {
        // Bei Bedarf implementieren
    }

    /**
     * Handle window resize events
     */
    public void resize(int width, int height) {
        // Update UI camera
        uiCamera.setToOrtho(false, width, height);
        
        // Update UI components if they need to know about size changes
        if (inventoryUI != null) {
            // Inventory UI might need to recalculate positions
        }
        if (craftingMenuUI != null) {
            // Crafting menu UI might need to recalculate positions
        }
        if (hotbarUI != null) {
            // Hotbar UI might need to recalculate positions
        }
    }
}
