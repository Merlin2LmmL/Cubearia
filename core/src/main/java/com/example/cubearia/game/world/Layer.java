package com.example.cubearia.game.world;

public class Layer {
    public final BlockType block;
    public final int height; // Anzahl an Schichten

    public Layer(BlockType block, int height) {
        this.block = block;
        this.height = height;
    }
}
