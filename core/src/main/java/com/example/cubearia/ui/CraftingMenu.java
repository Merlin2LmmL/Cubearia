package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CraftingMenu {
    private Stage stage;
    private Skin skin; // Eine Skin-Datei für UI-Elemente (kann als JSON definiert werden)

    public CraftingMenu() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        createMenu();
    }

    private void createMenu() {
        Table table = new Table();
        table.setFillParent(true);
        // Hier platzierst du z.B. Buttons und Labels, die die Crafting-Rezepte anzeigen.
        // table.add( ... );
        stage.addActor(table);
    }

    public Stage getStage() {
        return stage;
    }

    public void render() {
        stage.act();
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
