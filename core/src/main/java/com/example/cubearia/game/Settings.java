package com.example.cubearia.game;

import com.example.cubearia.game.inventory.Inventory;
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
    public static final boolean     RENDERALLBLOCKS = false;
    public static int               RENDERDISTANCE  = 1;

    // UI SETTINGS
    public static final float       UISCALE         = 75/100f;

    // OTHER
    public static final boolean     DEBUG           = false;
    public static final boolean     CREATIVE_MODE   = true;
}
