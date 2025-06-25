package com.example.cubearia.game.world;

import com.example.cubearia.game.Settings;

/**
 * Definiert verschiedene Erztypen mit ihren Spawn-Eigenschaften.
 * Jeder VainType beschreibt ein Erzvorkommen mit Höhenbereich, Wahrscheinlichkeit und Größe.
 * Höhen werden als Prozentsatz vom Meeresspiegel definiert (0% = Meeresspiegel, 100% = tiefster Punkt).
 */
public enum VainType {
    // Kohle - häufig, oberflächennah
    COAL(
        BlockType.COAL_ORE,
        -0.2f,  // minHeightPercent (10% vom Meeresspiegel)
        0.8f,  // maxHeightPercent (80% vom Meeresspiegel)
        0.8f,  // spawnProbability (80%)
        3,     // minSize
        8      // maxSize
    ),

    // Eisen - mittlere Häufigkeit, mittlere Tiefe
    IRON(
        BlockType.IRON_ORE,
        0.2f,  // minHeightPercent (20% vom Meeresspiegel)
        1f,  // maxHeightPercent (70% vom Meeresspiegel)
        0.6f,  // spawnProbability (60%)
        2,     // minSize
        6      // maxSize
    ),

    // Gold - selten, tief
    GOLD(
        BlockType.GOLD_ORE,
        0.3f,  // minHeightPercent (30% vom Meeresspiegel)
        1f,  // maxHeightPercent (90% vom Meeresspiegel)
        0.4f,  // spawnProbability (40%)
        1,     // minSize
        4      // maxSize
    ),

    // Diamant - sehr selten, sehr tief
    DIAMOND(
        BlockType.DIAMOND_ORE,
        0.7f,  // minHeightPercent (50% vom Meeresspiegel)
        1f, // maxHeightPercent (95% vom Meeresspiegel)
        0.15f, // spawnProbability (25%)
        3,     // minSize
        8      // maxSize
    );

    private final BlockType oreBlock;
    private final float minHeightPercent;
    private final float maxHeightPercent;
    private final float spawnProbability;
    private final int minSize;
    private final int maxSize;

    VainType(BlockType oreBlock, float minHeightPercent, float maxHeightPercent, float spawnProbability, int minSize, int maxSize) {
        this.oreBlock = oreBlock;
        this.minHeightPercent = minHeightPercent;
        this.maxHeightPercent = maxHeightPercent;
        this.spawnProbability = spawnProbability;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public BlockType getOreBlock() { return oreBlock; }
    public float getMinHeightPercent() { return minHeightPercent; }
    public float getMaxHeightPercent() { return maxHeightPercent; }
    public float getSpawnProbability() { return spawnProbability; }
    public int getMinSize() { return minSize; }
    public int getMaxSize() { return maxSize; }

    /**
     * Prüft, ob ein VainType an der gegebenen Höhe spawnen kann.
     * Höhen werden als Prozentsatz vom Meeresspiegel berechnet.
     */
    public boolean canSpawnAtHeight(int height) {
        // Berechne die relative Höhe vom Meeresspiegel (0% = Meeresspiegel, 100% = tiefster Punkt)
        float relativeHeight = (float)(Settings.SEA_LEVEL - height) / Settings.SEA_LEVEL;

        // Prüfe, ob die Höhe im erlaubten Bereich liegt
        return relativeHeight >= minHeightPercent && relativeHeight <= maxHeightPercent;
    }
}
