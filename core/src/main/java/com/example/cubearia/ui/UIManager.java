package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.example.cubearia.game.Assets;
import com.example.cubearia.game.entities.Player;

public class UIManager {
    private static Player player;
    private static OrthographicCamera uiCamera;

    private static InventoryUI inventoryUI;
    private static HotbarUI hotbarUI;

    public UIManager(Player player, OrthographicCamera uiCamera) {
        this.player = player;
        this.uiCamera = uiCamera;

        inventoryUI = new InventoryUI(player.getInventory());
        hotbarUI = new HotbarUI(player.getInventory()
        );
    }

    /** Maus gedrückt */
    public void pressClick(int screenX, int screenY, int button) {
        // schauen, dass es wirklich Linksklick ist und Inventar offen
        if (button == Input.Buttons.LEFT && player.inventoryOpen) {
            // NICHT hier unprojecten!
            inventoryUI.handleTouchDown(screenX, screenY, uiCamera);
        }
    }

    /** Maus losgelassen */
    public void releaseClick(int screenX, int screenY, int button) {
        if (button == Input.Buttons.LEFT && player.inventoryOpen) {
            inventoryUI.handleTouchUp(screenX, screenY, uiCamera);
        }
    }

    /** Diese Methode steuert nur das Rendern bzw. Maus‑Icon */
    public void updateMousePosition(float screenX, float screenY) {
        inventoryUI.updateMousePosition(screenX, screenY, uiCamera);
    }

    public static void render(SpriteBatch batch) {
        if (player.inventoryOpen) {
            inventoryUI.render(batch, uiCamera);
        } else {
            hotbarUI.render(batch, uiCamera, player.selectedSlot);
        }
    }
}
