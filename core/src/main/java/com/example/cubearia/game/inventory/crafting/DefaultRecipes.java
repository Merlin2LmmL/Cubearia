package com.example.cubearia.game.inventory.crafting;

import com.example.cubearia.game.world.BlockType;

public enum DefaultRecipes {
    WOOD_PLANK_1X1(
        new String[][] {{ "OAK_LOG" }},
        "OAK_PLANK", 4
    ),

    CRAFTING_TABLE(
        new String[][] {
            { "OAK_PLANK", "OAK_PLANK" },
            { "OAK_PLANK", "OAK_PLANK" }
        },
        "CRAFTING_TABLE", 1
    );

    private final String[][] pattern;
    private final String result;
    private final int amount;

    DefaultRecipes(String[][] pattern, String result, int amount) {
        this.pattern = pattern;
        this.result = result;
        this.amount = amount;
    }

    public String[][] getPattern() {
        return pattern;
    }

    public String getResult() {
        return result;
    }

    public int getAmount() {
        return amount;
    }

    public static void registerAll(RecipeRegistry registry) {
        for (DefaultRecipes r : values()) {
            registry.registerRecipe(r.getPattern(), r.getResult(), r.getAmount());
        }
    }
}
