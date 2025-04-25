package com.example.cubearia.game.inventory.crafting;

public class Recipe {
    private final String[][] pattern;
    private final String resultItem;

    public Recipe(String[][] pattern, String resultItem) {
        this.pattern = pattern;
        this.resultItem = resultItem;
    }

    public String[][] getPattern() {
        return pattern;
    }

    public String getResultItem() {
        return resultItem;
    }
}
