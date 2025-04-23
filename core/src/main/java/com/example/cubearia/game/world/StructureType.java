package com.example.cubearia.game.world;

import java.util.List;
import java.util.Random;

public enum StructureType {
    TREE            (List.of("maps/Tree.tmx")),
    CACTUS          (List.of("maps/Cacti.tmx")),
    ICE_SPIKE       (List.of("maps/Ice Spike.tmx")),
    LAVA_SEA        (List.of("maps/Lava Sea.tmx")),
    FLOATING_ICEBERG(List.of()),
    SUNKEN_SHIP     (List.of()),
    CORAL           (List.of()),
    CLAM_COLONY     (List.of()),
    ICE_FISHING_HOLE(List.of()),
    SEAGRASS        (List.of()),
    SEA_SHELL       (List.of()),
    IGLOO           (List.of()),
    SNOWMAN         (List.of()),
    SWAMP_HUT       (List.of()),
    MUD_POOL        (List.of()),
    HAY_BALE        (List.of()),
    BEEHIVE_TREE    (List.of()),
    FLOWER          (List.of()),
    GRASS           (List.of()),
    SAVANNA_TREE    (List.of()),
    JUNGLE_TREE     (List.of()),
    CACAO_TREE      (List.of()),
    DESERT_WELL     (List.of()),
    FOSSIL          (List.of()),
    HILL_CAVE       (List.of()),
    ABANDONED_CAMP  (List.of()),
    MINE_SHAFT      (List.of()),
    SCORCHED_TREE   (List.of()),
    VOLCANIC_VENT   (List.of()),
    FALLEN_TREE     (List.of());

    private final List<String> filePaths;
    private static final Random random = new Random();

    StructureType(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getRandomFilePath() {
        if (filePaths.isEmpty()) return null;
        return filePaths.get(random.nextInt(filePaths.size()));
    }
}
