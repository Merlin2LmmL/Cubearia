package com.example.cubearia;

import com.badlogic.gdx.Game;
import com.example.cubearia.game.GameScreen;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
