package com.example.cubearia.game.world;

public class Structure {
    private final StructureType type;
    private final int minDistance;
    private final float spawnProbability;
    private final int trunkXOffset;
    private final int trunkYOffset;

    public Structure(StructureType type, int minDistance, float spawnProbability, int trunkXOffset, int trunkYOffset) {
        this.type = type;
        this.minDistance = minDistance;
        this.spawnProbability = spawnProbability;
        this.trunkXOffset = trunkXOffset;
        this.trunkYOffset = trunkYOffset;
    }

    public String getFilePath() {
        return type.getRandomFilePath();
    }

    public StructureType getType() {
        return type;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public float getSpawnProbability() {
        return spawnProbability;
    }

    public int getTrunkXOffset() {
        return trunkXOffset;
    }

    public int getTrunkYOffset() {
        return trunkYOffset;
    }
}
