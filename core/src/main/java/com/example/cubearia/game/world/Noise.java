package com.example.cubearia.game.world;

import java.util.Random;
import com.example.cubearia.game.Settings;

/**
 * Generiert deterministische Rauschwerte basierend auf dem eingestellten Seed.
 * Perlin Noise
 */
public class Noise {
    private static final int SIZE = 4096;
    private static final float[] values = new float[SIZE];

    static {
        Random random = new Random(Settings.SEED);
        for (int i = 0; i < SIZE; i++) {
            values[i] = random.nextFloat();
        }
    }

    /**
     * Gibt interpolierte Rauschwerte zurück, basierend auf dem globalen x.
     */
    public static float getNoise(float x) {
        x *= 0.1f;
        int i = (int) Math.floor(x);
        float frac = x - i;

        float v1 = getValue(i);
        float v2 = getValue(i + 1);

        return smoothLerp(v1, v2, frac);
    }

    private static float getValue(int i) {
        return values[Math.floorMod(i, SIZE)];
    }

    private static float smoothLerp(float a, float b, float t) {
        // Smoothstep-Interpolation
        t = t * t * (3 - 2 * t);
        return a * (1 - t) + b * t;
    }

    /**
     * Ermittelt die Terrain-Höhe für globalX mit gegebener Schwankung.
     */
    public static float getHeight(int globalX, int fluctuation) {
        float noise = getNoise(globalX);
        return 64 + noise * fluctuation;
    }
}
