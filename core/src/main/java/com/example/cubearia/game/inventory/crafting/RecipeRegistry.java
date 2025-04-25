package com.example.cubearia.game.inventory.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;

public class RecipeRegistry {
    private static class InternalRecipe {
        private final String[][] pattern;
        private final String resultItem;
        private final int amount;
        InternalRecipe(String[][] pattern, String resultItem, int amount) {
            this.pattern = pattern;
            this.resultItem = resultItem;
            this.amount = amount;
        }
    }

    private final List<InternalRecipe> recipes = new ArrayList<>();

    /**
     * Registriert ein neues Rezept und loggt es zur Debug-Ausgabe.
     */
    public void registerRecipe(String[][] pattern, String resultItem, int amount) {
        recipes.add(new InternalRecipe(pattern, resultItem, amount));
    }

    /**
     * Versucht, ein Rezept für das gegebene Grid zu finden.
     * Liefert den resultItem-Namen oder null, falls kein Rezept passt.
     */
    public String matchRecipe(String[][] inputGrid) {
        int inRows = inputGrid.length;
        int inCols = inputGrid[0].length;

        for (InternalRecipe r : recipes) {
            int patRows = r.pattern.length;
            int patCols = r.pattern[0].length;
            if (patRows > inRows || patCols > inCols) {
                continue;
            }
            for (int yOff = 0; yOff <= inRows - patRows; yOff++) {
                for (int xOff = 0; xOff <= inCols - patCols; xOff++) {
                    if (matchesAt(r.pattern, inputGrid, yOff, xOff)) {
                        return r.resultItem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prüft, ob das Pattern an gegebener Offset-Position zum Grid passt.
     */
    private boolean matchesAt(String[][] pattern, String[][] grid, int offY, int offX) {
        int patRows = pattern.length;
        int patCols = pattern[0].length;
        int inRows = grid.length;
        int inCols = grid[0].length;
        // 1) Pattern-Bereich prüfen
        for (int y = 0; y < patRows; y++) {
            for (int x = 0; x < patCols; x++) {
                String expected = pattern[y][x];
                String actual = grid[y + offY][x + offX];
                if (!Objects.equals(expected, actual)) {
                    return false;
                }
            }
        }
        // 2) Außerhalb Pattern muss null sein
        for (int y = 0; y < inRows; y++) {
            for (int x = 0; x < inCols; x++) {
                boolean inZone = y >= offY && y < offY + patRows &&
                    x >= offX && x < offX + patCols;
                if (!inZone && grid[y][x] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int matchRecipeAmount(String[][] inputGrid) {
        int inRows = inputGrid.length;
        int inCols = inputGrid[0].length;

        for (InternalRecipe r : recipes) {
            int patRows = r.pattern.length;
            int patCols = r.pattern[0].length;
            if (patRows > inRows || patCols > inCols) {
                continue;
            }
            for (int yOff = 0; yOff <= inRows - patRows; yOff++) {
                for (int xOff = 0; xOff <= inCols - patCols; xOff++) {
                    if (matchesAt(r.pattern, inputGrid, yOff, xOff)) {
                        return r.amount;
                    }
                }
            }
        }
        return 0;
    }
}
