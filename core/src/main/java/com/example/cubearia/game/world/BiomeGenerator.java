package com.example.cubearia.game.world;

/**
 * Interface für Biome-Generatoren.
 */
public interface BiomeGenerator {
    /**
     * Liefert den BiomeType an der globalen X‑Position.
     */
    BiomeType getBiomeAt(int x);
}
