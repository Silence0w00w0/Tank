package com.silence.tank;

import java.util.List;

public record EnemyWave(EnemyType type, int count, float spawnEvery, List<GridCoord> spawnPoints) {
    public EnemyWave {
        if (count < 0) {
            throw new IllegalArgumentException("Enemy count cannot be negative.");
        }
        if (spawnEvery <= 0f) {
            throw new IllegalArgumentException("Spawn interval must be positive.");
        }
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            throw new IllegalArgumentException("At least one spawn point is required.");
        }
        spawnPoints = List.copyOf(spawnPoints);
    }
}
