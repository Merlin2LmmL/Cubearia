package com.example.cubearia.game.world;

public class NoiseBasedBiomeGenerator implements BiomeGenerator {

    @Override
    public BiomeType getBiomeAt(int x) {
        // Biome-Kategorien sollen sich langsam ändern:
        float categoryNoise = Noise.getNoise(x * 0.04f);
        float temperatureNoise = Noise.getNoise(x * 0.04f + 10000);

        BiomeGroup group = getGroup(categoryNoise);
        float temp = temperatureNoise;

        return pickBiome(group, temp);
    }

    private enum BiomeGroup {
        OCEAN, SHALLOW_OCEAN, BEACH, STANDART, HILL, MOUNTAIN
    }

    private BiomeGroup getGroup(float value) {
        if (value < 0.20f) return BiomeGroup.OCEAN;
        if (value < 0.25f) return BiomeGroup.SHALLOW_OCEAN;
        if (value < 0.30f) return BiomeGroup.BEACH;
        if (value < 0.66f) return BiomeGroup.STANDART;
        if (value < 0.76f) return BiomeGroup.HILL;
        return BiomeGroup.MOUNTAIN;
    }

    private BiomeType pickBiome(BiomeGroup group, float temp) {
        if (group == BiomeGroup.OCEAN) {
            if (temp < 0.33f) return BiomeType.COLD_OCEAN;
            if (temp < 0.66f) return BiomeType.OCEAN;
            return BiomeType.WARM_OCEAN;
        }

        if (group == BiomeGroup.SHALLOW_OCEAN) {
            if (temp < 0.33) return BiomeType.COLD_SHALLOW_OCEAN;
            if (temp < 0.66) return BiomeType.SHALLOW_OCEAN;
            return BiomeType.WARM_SHALLOW_OCEAN;
        }

        if (group == BiomeGroup.BEACH) {
            if (temp < 0.33) return BiomeType.COLD_BEACH;
            if (temp < 0.66) return BiomeType.BEACH;
            return BiomeType.WARM_BEACH;
        }

        if (group == BiomeGroup.STANDART) {
            if (temp < 0.1f) return BiomeType.ICE_SPIKES;
            if (temp < 0.2f) return BiomeType.SNOWY_TAIGA;
            if (temp < 0.3f) return BiomeType.SNOWY_PLAINS;
            if (temp < 0.4f) return BiomeType.SWAMP;
            if (temp < 0.5f) return BiomeType.PLAINS;
            if (temp < 0.6f) return BiomeType.FOREST;
            if (temp < 0.7f) return BiomeType.FLOWER_FOREST;
            if (temp < 0.8f) return BiomeType.SAVANNA;
            return BiomeType.DESERT;
        }

        if (group == BiomeGroup.HILL) {
            if (temp < 0.33) return BiomeType.COLD_HILL;
            if (temp < 0.66) return BiomeType.HILL;
            return BiomeType.BADLANDS;
        }

        // group == MOUNTAIN
        if (temp < 0.33f) return BiomeType.SNOWY_MOUNTAIN;
        if (temp < 0.66f) return BiomeType.MOUNTAIN;
        return BiomeType.VOLCANIC_MOUNTAIN;
    }
}
