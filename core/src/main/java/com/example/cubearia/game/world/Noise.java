package com.example.cubearia.game.world;

import java.util.Random;

public class Noise {
    private static final int SIZE = 4096;
    private static final float[] values = new float[SIZE];
    private static final int seed = (int)(System.currentTimeMillis() % Integer.MAX_VALUE); // beeinflusst Terrain bei Spielstart

    static {
        Random random = new Random(seed);
        for (int i = 0; i < SIZE; i++) {
            values[i] = random.nextFloat();
        }
    }

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
        t = t * t * (3 - 2 * t); // Smoothstep
        return a * (1 - t) + b * t;
    }

    public static float getHeight(int globalX, int fluctuation) {
        float noise = getNoise(globalX);
        float height = 64 + noise * fluctuation; // Basis + Schwankung
        return height;
    }
}
