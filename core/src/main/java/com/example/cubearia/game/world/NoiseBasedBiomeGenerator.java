package com.example.cubearia.game.world;

import com.example.cubearia.game.Settings;

import java.util.Arrays;
import java.util.Comparator;

public class NoiseBasedBiomeGenerator implements BiomeGenerator {

    @Override
    public BiomeType getBiomeAt(int x) {
        if (Settings.ONLY_BIOME == null) {
            float categoryNoise = Noise.getNoise(x * 0.04f);
            float temperatureNoise = Noise.getNoise(x * 0.04f + 10000);

            // Best passende Gruppe anhand categoryValue
            BiomeType.BiomeGroup bestGroup = Arrays.stream(BiomeType.values())
                .min(Comparator.comparingDouble(b -> Math.abs(b.getCategoryValue() - categoryNoise)))
                .map(BiomeType::getGroup)
                .orElse(BiomeType.BiomeGroup.STANDARD);

            // Innerhalb dieser Gruppe Biom mit bestmöglicher Temperatur wählen
            return Arrays.stream(BiomeType.values())
                .filter(b -> b.getGroup() == bestGroup)
                .min(Comparator.comparingDouble(b -> Math.abs(b.getTemperatureValue() - temperatureNoise)))
                .orElse(BiomeType.PLAINS); // Fallback
        } else return Settings.ONLY_BIOME;
    }
}
