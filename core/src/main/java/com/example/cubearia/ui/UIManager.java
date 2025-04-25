package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.entities.Player;
import com.example.cubearia.game.inventory.Inventory;
import com.example.cubearia.game.inventory.crafting.DefaultRecipes;
import com.example.cubearia.game.inventory.crafting.RecipeRegistry;
import com.example.cubearia.game.world.World;

public class UIManager {
    private final Player player;
    private final OrthographicCamera uiCamera;
    private final World world;
    private final Inventory inventory;
    private final RecipeRegistry recipeRegistry;
    private final InventoryUI inventoryUI;
    private final HotbarUI hotbarUI;

    /**
     * Konstruktor initialisiert alle UI-Komponenten und lädt Rezepte.
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

        // UI-Komponenten initialisieren
        this.inventoryUI = new InventoryUI(inventory, recipeRegistry);
        this.hotbarUI    = new HotbarUI(inventory);
    }

    /**
     * Wird aufgerufen, wenn die Maustaste gedrückt wird.
     */
    public void pressClick(int screenX, int screenY, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT && player.inventoryOpen) {
            inventoryUI.handleTouchDown(screenX, screenY, uiCamera, button == Input.Buttons.LEFT);
        }
    }

    /**
     * Wird aufgerufen, wenn die Maustaste losgelassen wird.
     */
    public void releaseClick(int screenX, int screenY, int button) {
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT && player.inventoryOpen) {
            inventoryUI.handleTouchUp(screenX, screenY, uiCamera);
        }
    }

    /**
     * Toggeln des Inventar-Zustands (öffnen/schließen).
     */
    public void toggleInventory() {
        player.inventoryOpen = !player.inventoryOpen;
        player.inMenu       = player.inventoryOpen;
        if (!player.inventoryOpen) {
            // Droppt Crafting-Items ins World und leert Crafting-Felder
            inventory.dropCraftingAndClear(player, world);
        }
    }

    /**
     * Update der Maus-Position (z.B. für benutzerdefiniertes Cursor-Rendering).
     */
    public void updateMousePosition(float screenX, float screenY) {
        // aktuell leer, kann für Cursor/Tooltip-Nachführung genutzt werden
    }

    /**
     * Render-Methode: zeichnet entweder Inventar-UI oder Hotbar-UI.
     */
    public void render(SpriteBatch batch) {
        if (player.inventoryOpen) {
            inventoryUI.render(batch, uiCamera);
        } else {
            hotbarUI.render(batch, uiCamera, player.selectedSlot);
        }
    }
}
