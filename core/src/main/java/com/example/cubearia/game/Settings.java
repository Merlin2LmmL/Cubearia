package com.example.cubearia.game;

import com.example.cubearia.game.world.BiomeType;
import com.example.cubearia.game.world.Chunk;

import java.security.SecureRandom;

public class Settings {
    // WORLD SETTINGS
    public static final boolean     FLATWORLD       = false;
    public static final boolean     STRUCTURES      = true;
    public static final long        SEED            = new SecureRandom().nextLong();
    public static final BiomeType   ONLY_BIOME      = null;
    public static final int         SEA_LEVEL       = Chunk.HEIGHT - 66;

    // RENDER SETTINGS
    public static final boolean     USE_RAYCASTING_VISIBILITY = false;
    public static final boolean     RENDERALLBLOCKS = true;
    public static final int         MAX_ZOOM        = 32;
    public static int               RENDERDISTANCE;

    // MOB SPAWNING SETTINGS
    public static final float       MOB_SPAWN_RATE_PER_SECOND = 0.5f; // How many spawn attempts per second
    public static final int         MOB_SPAWN_RADIUS = 20; // Radius around player to check for spawn positions
    public static final int         MAX_MOBS_NEAR_PLAYER = 10; // Maximum mobs within spawn radius

    // UI SETTINGS
    public static final float       UISCALE         = 75/100f;

    // OTHER
    public static final boolean     DEBUG           = false;
    public static final boolean     CREATIVE_MODE   = false;
    public static final boolean     VSYNC           = false;
}
