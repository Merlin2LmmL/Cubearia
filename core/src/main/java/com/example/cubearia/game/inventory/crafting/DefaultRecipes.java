package com.example.cubearia.game.inventory.crafting;

import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemRegistry;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.world.BlockType;

public enum DefaultRecipes {
    WOOD_PLANK(
        new String[][] {{ "OAK_LOG" }},
        "OAK_PLANK", 4
    ),

    CRAFTING_TABLE(
        new String[][] {
            { "OAK_PLANK", "OAK_PLANK" },
            { "OAK_PLANK", "OAK_PLANK" }
        },
        "CRAFTING_TABLE", 1
    ),

    STICK(
        new String[][] {
            { "OAK_PLANK" },
            { "OAK_PLANK" }
        },
        "STICK", 4
    ),

    WOODEN_PICKAXE(
        new String[][] {
            { "OAK_PLANK", "OAK_PLANK", "OAK_PLANK" },
            { null,        "STICK",     null       },
            { null,        "STICK",     null       }
        },
        "WOODEN_PICKAXE", 1
    );

    private final String[][] pattern;
    private final Item result;
    private final int amount;

    DefaultRecipes(String[][] pattern, String result, int amount) {
        this.pattern = pattern;
        this.result = Item.valueOf(result);
        this.amount = amount;
    }

    public String[][] getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return new ItemStack(result, amount);
    }

    public int getAmount() {
        return amount;
    }

    public static void registerAll(RecipeRegistry registry) {
        for (DefaultRecipes r : values()) {
            registry.registerRecipe(r.getPattern(), r.getResult());
        }
    }
}
