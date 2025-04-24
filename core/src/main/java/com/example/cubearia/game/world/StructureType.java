package com.example.cubearia.game.world;

import java.util.List;
import java.util.Random;

public enum StructureType {
    TREE            (List.of("maps/tree1.tmx", "maps/tree2.tmx", "maps/tree3.tmx")),
    CACTUS          (List.of("maps/cactus1.tmx", "maps/cactus2.tmx", "maps/cactus3.tmx")),
    ICE_SPIKE       (List.of("maps/ice_spike1.tmx", "maps/ice_spike2.tmx", "maps/ice_spike3.tmx", "maps/ice_spike4.tmx")),
    LAVA_SEA        (List.of("maps/lava1.tmx")),
    FLOATING_ICEBERG(List.of("maps/ice_berg1.tmx", "maps/ice_berg2.tmx")),
    SUNKEN_SHIP     (List.of("maps/sunken_ship1.tmx", "maps/sunken_ship2.tmx", "maps/sunken_ship3.tmx")),
    CORAL           (List.of("maps/coral1.tmx", "maps/coral2.tmx", "maps/coral3.tmx", "maps/coral4.tmx")),
    ICE_FISHING_HOLE(List.of("maps/fishing_hole1.tmx", "maps/fishing_hole2.tmx", "maps/fishing_hole3.tmx")),
    SEAGRASS        (List.of("maps/seagrass1.tmx", "maps/seagrass2.tmx", "maps/seagrass3.tmx")),
    SEA_SHELL       (List.of("maps/seashell1.tmx", "maps/seashell2.tmx")),
    IGLOO           (List.of("maps/igloo1.tmx", "maps/igloo2.tmx")),
    SNOWMAN         (List.of("maps/snowman1.tmx", "maps/snowman2.tmx")),
    SWAMP_HUT       (List.of("maps/swamp_hut1.tmx")),
    MUD_POOL        (List.of("maps/mud_hole1.tmx", "maps/mud_hole2.tmx", "maps/mud_hole3.tmx")),
    FLOWER          (List.of("maps/flower1.tmx", "maps/flower2.tmx", "maps/flower3.tmx")),
    GRASS           (List.of("maps/grass1.tmx")),
    SAVANNA_TREE    (List.of("maps/savanna_tree1.tmx", "maps/savanna_tree2.tmx", "maps/savanna_tree3.tmx")),
    JUNGLE_TREE     (List.of("maps/jungle_tree1.tmx", "maps/jungle_tree2.tmx", "maps/jungle_tree3.tmx", "maps/jungle_tree4.tmx")),
    HUGE_JUNGLE_TREE(List.of("maps/jungle_tree5.tmx", "maps/jungle_tree6.tmx")),
    DESERT_WELL     (List.of("maps/desert_well1.tmx")),
    FOSSIL          (List.of("maps/fossil1.tmx", "maps/fossil2.tmx", "maps/fossil3.tmx", "maps/fossil4.tmx")),
    MINE_SHAFT      (List.of("maps/mineshaft1.tmx", "maps/mineshaft2.tmx", "maps/mineshaft3.tmx", "maps/mineshaft4.tmx")),
    SCORCHED_TREE   (List.of("maps/scorched_tree1.tmx", "maps/scorched_tree2.tmx", "maps/scorched_tree3.tmx", "maps/scorched_tree4.tmx", "maps/scorched_tree5.tmx", "maps/scorched_tree6.tmx")),
    VOLCANIC_VENT   (List.of("maps/volcanic_vent1.tmx", "maps/volcanic_vent2.tmx"));

    private final List<String> filePaths;
    private static final Random random = new Random();

    StructureType(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getRandomFilePath() {
        return filePaths.get(random.nextInt(filePaths.size()));
    }

}
