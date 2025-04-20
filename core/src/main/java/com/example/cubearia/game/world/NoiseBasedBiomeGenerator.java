package com.example.cubearia.game.world;

public class NoiseBasedBiomeGenerator implements BiomeGenerator {
    private static final float TYPE_SCALE    = 0.2f;    // Skaliert X für Biome-Noise
    private static final float TYPE_OFFSET   = 0f;
    private static final float TEMP_SCALE    = 0.03f;
    private static final float TEMP_OFFSET   = 1000f;
    private static final float GAMMA         = 1.5f;    // >1 schwächt den Mittelbereich ab

    public static float OCEAN_WEIGHT    = 0.3f;
    public static float NORMAL_WEIGHT   = 0.4f;
    public static float MOUNTAIN_WEIGHT = 0.3f;

    @Override
    public BiomeType getBiomeAt(int globalX) {
        // 1) Noise in [-1..1]
        float raw = Noise.getNoise(globalX * TYPE_SCALE + TYPE_OFFSET);
        // 2) Map auf [0..1]
        float t01 = raw * 0.5f + 0.5f;
        // 3) clamp aus Sicherheitsgründen
        t01 = Math.min(Math.max(t01, 0f), 1f);
        // 4) Gamma-Korrektur
        t01 = (float) Math.pow(t01, 1.0f / GAMMA);

        // 5) Schwellen nach Gewichten
        if (t01 < OCEAN_WEIGHT) {
            return BiomeType.OCEAN;
        } else if (t01 < OCEAN_WEIGHT + NORMAL_WEIGHT) {
            // zusätzlich Temp-basiert Unterarten für normale Biome
            return pickNormalSubBiome(globalX);
        } else {
            return pickMountainSubBiome(globalX);
        }
    }

    private BiomeType pickNormalSubBiome(int globalX) {
        float rawTemp = Noise.getNoise(globalX * TEMP_SCALE + TEMP_OFFSET) * 2.5f;
        float temp    = Math.min(Math.max(rawTemp, 0f), 1f);

        if (temp > 0.90f)      return BiomeType.DESERT;
        else if (temp > 0.80f) return BiomeType.BADLANDS;
        else if (temp > 0.70f) return BiomeType.SAVANNA;
        else if (temp > 0.60f) return BiomeType.FORREST;
        else if (temp > 0.40f) return BiomeType.PLAINS;
        else if (temp > 0.30f) return BiomeType.FLOWER_FORREST;
        else if (temp > 0.20f) return BiomeType.SWAMP;
        else if (temp > 0.10f) return BiomeType.SNOWY_TAIGA;
        else if (temp > 0.05f) return BiomeType.SNOWY_PLAINS;
        else                   return BiomeType.ICE_SPICES;
    }

    private BiomeType pickMountainSubBiome(int globalX) {
        float rawTemp = Noise.getNoise(globalX * TEMP_SCALE + TEMP_OFFSET) * 2.5f;
        float temp    = Math.min(Math.max(rawTemp, 0f), 1f);

        if (temp > 0.75f)      return BiomeType.VOLCANIC_MOUNTAIN;
        else if (temp < 0.25f) return BiomeType.SNOWY_MOUNTAIN;
        else                   return BiomeType.MOUNTAIN;
    }
}
