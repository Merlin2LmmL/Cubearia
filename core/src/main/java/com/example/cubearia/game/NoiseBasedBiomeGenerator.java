package com.example.cubearia.game;

public class NoiseBasedBiomeGenerator implements BiomeGenerator {
    private static final float TYPE_SCALE = 0.05f;
    private static final float TEMP_SCALE = 0.5f;
    private static final float TYPE_OFFSET = 0f;
    private static final float TEMP_OFFSET = 1000f;
    public static float OCEAN_WEIGHT = 0.1f;
    public static float NORMAL_WEIGHT = 0.5f;
    public static float MOUNTAIN_WEIGHT = 0.4f;


    // Bestimme die Häufigkeit der Änderung von Biomen
    public static float biomeScale = 0.1f; // <- kannst du z.B. von außen ändern für Test

    @Override
    public BiomeType getBiomeAt(int globalX) {
        // 1) Grober Typ
        float rawType = Noise.getNoise(globalX * biomeScale + TYPE_OFFSET);
        float clampedType = Math.min(Math.max(rawType, 0f), 1f);

        int typeIndex;
        if (clampedType < OCEAN_WEIGHT) {
            typeIndex = 0;
        } else if (clampedType < OCEAN_WEIGHT + NORMAL_WEIGHT) {
            typeIndex = 1;
        } else {
            typeIndex = 2;
        }

        // 2) Temperatur in [0..1]
        float rawTemp = Noise.getNoise(globalX * TEMP_SCALE + TEMP_OFFSET) * 2.5f;
        float temp = Math.min(Math.max(rawTemp, 0f), 1f);

        // 3) Biome-Zuweisung
        if (typeIndex == 0) {
            return BiomeType.OCEAN;
        } else if (typeIndex == 1) {
            if (temp > 0.90f)        return BiomeType.DESERT;
            else if (temp > 0.80f)   return BiomeType.BADLANDS;
            else if (temp > 0.70f)   return BiomeType.SAVANNA;
            else if (temp > 0.60f)   return BiomeType.JUNGLE;
            else if (temp > 0.50f)   return BiomeType.FORREST;
            else if (temp > 0.40f)   return BiomeType.PLAINS;
            else if (temp > 0.30f)   return BiomeType.FLOWER_FORREST;
            else if (temp > 0.20f)   return BiomeType.SWAMP;
            else if (temp > 0.10f)   return BiomeType.SNOWY_TAIGA;
            else if (temp > 0.05f)   return BiomeType.SNOWY_PLAINS;
            else                     return BiomeType.ICE_SPICES;
        } else {
            if (temp > 0.75f)        return BiomeType.VOLCANIC_MOUNTAIN;
            else if (temp < 0.25f)   return BiomeType.SNOWY_MOUNTAIN;
            else                     return BiomeType.MOUNTAIN;
        }
    }
}
