package com.example.cubearia.game.inventory.crafting;

import com.example.cubearia.game.inventory.ItemStack;

import java.util.*;

/**
 * Universelle Recipe-Registry, die beliebig große Patterns gegen ein 3×3-Grid matcht.
 * Leere Slots im Pattern sind genau = null.
 * Jetzt mit Tag-Support: Einträge im Pattern, die einem Tag-Namen entsprechen,
 * werden gegen alle registrierten Items dieses Tags gematcht.
 */
public class RecipeRegistry {
    private static class InternalRecipe {
        final String[][] pattern;
        final ItemStack resultItem;

        InternalRecipe(String[][] pattern, ItemStack resultItem) {
            this.pattern = pattern;
            this.resultItem = resultItem;
        }
    }

    private final List<InternalRecipe> recipes = new ArrayList<>();
    // Map für Tag-Namen (z.B. "WOOD") zu einer Menge von Item-IDs
    private final Map<String, Set<String>> tagMap = new HashMap<>();

    /**
     * Registriert ein neues Tag mit einer anfänglichen Menge von Item-IDs.
     */
    public void registerTag(String tagName, Collection<String> itemIds) {
        tagMap.put(tagName.toUpperCase(Locale.ROOT), new HashSet<>(itemIds));
    }

    /**
     * Fügt einem bestehenden Tag einen weiteren Block/Item hinzu.
     */
    public void addItemToTag(String tagName, String itemId) {
        tagMap.computeIfAbsent(tagName.toUpperCase(Locale.ROOT), k -> new HashSet<>())
            .add(itemId);
    }

    public void registerRecipe(String[][] pattern, ItemStack resultItem) {
        recipes.add(new InternalRecipe(pattern, resultItem));
    }

    public ItemStack matchRecipeWithAutoPadding(String[][] inputGrid) {
        String[][] grid3x3 = padTo3x3(inputGrid);
        grid3x3 = flipVertical(grid3x3);
        return matchRecipe(grid3x3);
    }

    private String[][] padTo3x3(String[][] in) {
        String[][] out = new String[3][3];
        int r = in.length, c = in[0].length;
        for (int y = 0; y < r && y < 3; y++) {
            for (int x = 0; x < c && x < 3; x++) {
                out[y][x] = in[y][x];
            }
        }
        return out;
    }

    private String[][] flipVertical(String[][] in) {
        int rows = in.length, cols = in[0].length;
        String[][] out = new String[rows][cols];
        for (int y = 0; y < rows; y++) {
            out[y] = in[rows - 1 - y].clone();
        }
        return out;
    }

    private ItemStack matchRecipe(String[][] grid) {
        int rows = grid.length, cols = grid[0].length;
        for (InternalRecipe r : recipes) {
            String[][] pat = r.pattern;
            int pRows = pat.length, pCols = pat[0].length;
            if (pRows > rows || pCols > cols) continue;
            for (int yOff = 0; yOff <= rows - pRows; yOff++) {
                for (int xOff = 0; xOff <= cols - pCols; xOff++) {
                    if (matchesAt(pat, grid, yOff, xOff)) {
                        return new ItemStack(r.resultItem.getItem(), r.resultItem.getAmount());
                    }
                }
            }
        }
        return null;
    }

    private boolean matchesAt(String[][] pattern, String[][] grid, int offY, int offX) {
        int rows = grid.length, cols = grid[0].length;
        int pRows = pattern.length, pCols = pattern[0].length;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int py = y - offY, px = x - offX;
                boolean inPattern = py >= 0 && py < pRows && px >= 0 && px < pCols;
                String expected = inPattern ? pattern[py][px] : null;
                String actual   = grid[y][x];

                if (expected == null) {
                    if (actual != null) return false;
                } else {
                    // Tag-Abgleich: ist expected ein registriertes Tag?
                    String key = expected.toUpperCase(Locale.ROOT);
                    if (tagMap.containsKey(key)) {
                        Set<String> allowed = tagMap.get(key);
                        if (actual == null || !allowed.contains(actual)) {
                            return false;
                        }
                    } else {
                        // Normales String-Matching
                        if (actual == null || !expected.equalsIgnoreCase(actual)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
