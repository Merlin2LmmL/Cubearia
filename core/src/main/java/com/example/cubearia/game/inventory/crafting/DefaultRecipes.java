package com.example.cubearia.game.inventory.crafting;

import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemRegistry;
import com.example.cubearia.game.inventory.ItemStack;
import com.example.cubearia.game.world.BlockType;
import java.util.Arrays;
import java.util.Collections;

public enum DefaultRecipes {
    OAK_PLANK(
        new String[][] {{ "OAK_LOG" }},
        "OAK_PLANK", 4
    ),

    ACACIA_PLANK(
        new String[][] {{ "ACACIA_LOG" }},
        "OAK_PLANK", 4
    ),

    JUNGLE_PLANK(
        new String[][] {{ "JUNGLE_LOG" }},
        "OAK_PLANK", 4
    ),

    CRAFTING_TABLE(
        new String[][] {
            { "plank", "plank" },
            { "plank", "plank" }
        },
        "CRAFTING_TABLE", 1
    ),

    STICK(
        new String[][] {
            { "plank" },
            { "plank" }
        },
        "STICK", 4
    ),

    WOODEN_SWORD(
        new String[][] {
            { "plank" },
            { "plank" },
            { "STICK" }
        },
        "WOODEN_SWORD", 1
    ),

    WOODEN_PICKAXE(
        new String[][] {
            { "plank", "plank", "plank" },
            { null,    "STICK",  null       },
            { null,    "STICK",  null       }
        },
        "WOODEN_PICKAXE", 1
    ),

    WOODEN_AXE1(
        new String[][] {
            { "plank",    "plank"  },
            { "STICK",    "plank"  },
            { "STICK",    null     }
        },
        "WOODEN_AXE", 1
    ),

    WOODEN_AXE2(
        new String[][] {
            { "plank", "plank" },
            { "plank", "STICK" },
            { null,    "STICK" }
        },
        "WOODEN_AXE", 1
    ),

    WOODEN_SHOVEL(
        new String[][] {
            { "plank" },
            { "STICK" },
            { "STICK" }
        },
        "WOODEN_SHOVEL", 1
    ),

    WOODEN_HOE1(
        new String[][] {
            { "plank",    "plank"  },
            { "STICK",    null     },
            { "STICK",    null     }
        },
        "WOODEN_HOE", 1
    ),

    WOODEN_HOE2(
        new String[][] {
            { "plank", "plank" },
            { null,    "STICK" },
            { null,    "STICK" }
        },
        "WOODEN_HOE", 1
    ),

    STONE_SWORD(
        new String[][] {
            { "stone" },
            { "stone" },
            { "STICK" }
        },
        "STONE_SWORD", 1
    ),

    STONE_PICKAXE(
        new String[][] {
            { "stone", "stone", "stone" },
            { null,    "STICK",  null       },
            { null,    "STICK",  null       }
        },
        "STONE_PICKAXE", 1
    ),

    STONE_AXE1(
        new String[][] {
            { "stone",    "stone"  },
            { "STICK",    "stone"  },
            { "STICK",    null     }
        },
        "STONE_AXE", 1
    ),

    STONE_AXE2(
        new String[][] {
            { "stone", "stone" },
            { "stone", "STICK" },
            { null,    "STICK" }
        },
        "STONE_AXE", 1
    ),

    STONE_SHOVEL(
        new String[][] {
            { "stone" },
            { "STICK" },
            { "STICK" }
        },
        "STONE_SHOVEL", 1
    ),

    STONE_HOE1(
        new String[][] {
            { "stone",    "stone"  },
            { "STICK",    null     },
            { "STICK",    null     }
        },
        "STONE_HOE", 1
    ),

    STONE_HOE2(
        new String[][] {
            { "stone", "stone" },
            { null,    "STICK" },
            { null,    "STICK" }
        },
        "STONE_HOE", 1
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
        registry.registerTag("plank", Arrays.asList(
            "OAK_PLANK",
            "BIRCH_PLANK",
            "SPRUCE_PLANK"
        ));

        registry.registerTag("stone", Arrays.asList(
            "COBBLESTONE",
            "BLACKSTONE",   // (FOR FUTURE)
            "COBBLED_DEEPSLATE"
        ));

        for (DefaultRecipes r : values()) {
            registry.registerRecipe(r.getPattern(), r.getResult());
        }
    }
}
