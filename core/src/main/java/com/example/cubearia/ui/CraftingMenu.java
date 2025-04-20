package com.example.cubearia.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CraftingMenu {
    private Stage stage;
    private Skin skin;

    public CraftingMenu() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        createMenu();
    }

    private void createMenu() {
        Table table = new Table();
        table.setFillParent(true);
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
