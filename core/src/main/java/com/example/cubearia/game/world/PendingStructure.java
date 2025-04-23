package com.example.cubearia.game.world;

public class PendingStructure {
    public String file;
    public int globalX, globalY;

    public PendingStructure(String file, int globalX, int globalY) {
        this.file = file;
        this.globalX = globalX;
        this.globalY = globalY;
    }
}
