package com.silence.tank;

public enum EnemyType {
    BASIC(1, 82f, 100, false, AssetKeys.ENEMY_BASIC),
    FAST(1, 118f, 150, false, AssetKeys.ENEMY_FAST),
    ARMORED(3, 72f, 250, false, AssetKeys.ENEMY_ARMORED),
    POWER(2, 88f, 220, true, AssetKeys.ENEMY_POWER);

    public final int health;
    public final float speed;
    public final int score;
    public final boolean powerShot;
    public final String region;

    EnemyType(int health, float speed, int score, boolean powerShot, String region) {
        this.health = health;
        this.speed = speed;
        this.score = score;
        this.powerShot = powerShot;
        this.region = region;
    }
}
