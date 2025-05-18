package com.example.cubearia.game.world;

import java.util.Random;

/**
 * Simplex Noise 2D implementation
 * Public domain code by Stefan Gustavson, ported and simplified for Java.
 */
public class SimplexNoise {
    private static final int[] PERM = new int[512];
    private static final int[] PERM_MOD_12 = new int[512];
    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    private static final double G2 = (3.0 - SQRT_3) / 6.0;

    private static final int[][] GRAD3 = {
        {1,1}, {-1,1}, {1,-1}, {-1,-1},
        {1,0}, {-1,0}, {1,0}, {-1,0},
        {0,1}, {0,-1}, {0,1}, {0,-1}
    };

    public SimplexNoise(long seed) {
        Random rand = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // Shuffle
        for (int i = 255; i > 0; i--) {
            int swap = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[swap];
            p[swap] = temp;
        }
        // Duplicate into PERM arrays
        for (int i = 0; i < 512; i++) {
            PERM[i] = p[i & 255];
            PERM_MOD_12[i] = PERM[i] % 12;
        }
    }

    public double eval(double xin, double yin) {
        double n0, n1, n2;

        double s = (xin + yin) * F2;
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);

        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = xin - X0;
        double y0 = yin - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1; j1 = 0;
        } else {
            i1 = 0; j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;

        int ii = i & 255;
        int jj = j & 255;
        int gi0 = PERM_MOD_12[ii + PERM[jj]];
        int gi1 = PERM_MOD_12[ii + i1 + PERM[jj + j1]];
        int gi2 = PERM_MOD_12[ii + 1 + PERM[jj + 1]];

        n0 = contribution(x0, y0, gi0);
        n1 = contribution(x1, y1, gi1);
        n2 = contribution(x2, y2, gi2);

        return 70.0 * (n0 + n1 + n2); // Normalized result in [-1, 1]
    }

    private double contribution(double x, double y, int gi) {
        double t = 0.5 - x * x - y * y;
        if (t < 0) return 0.0;
        t *= t;
        return t * t * dot(GRAD3[gi], x, y);
    }

    private int fastFloor(double x) {
        return x > 0 ? (int) x : (int) x - 1;
    }

    private double dot(int[] g, double x, double y) {
        return g[0] * x + g[1] * y;
    }
}
