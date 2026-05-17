package com.silence.tank;

import com.badlogic.gdx.math.Rectangle;

public final class Tank {
    private final boolean player;
    private final EnemyType enemyType;
    private float x;
    private float y;
    private Direction direction = Direction.UP;
    private int health;
    private int lives;
    private float fireCooldown;
    private float aiDecisionTimer;
    private float shieldTimer;
    private float speedTimer;
    private float powerShotTimer;
    private boolean alive = true;

    private Tank(boolean player, EnemyType enemyType, float x, float y, int health, int lives) {
        this.player = player;
        this.enemyType = enemyType;
        this.x = x;
        this.y = y;
        this.health = health;
        this.lives = lives;
    }

    public static Tank player(float x, float y, int lives) {
        return new Tank(true, null, x, y, 1, lives);
    }

    public static Tank enemy(EnemyType type, float x, float y) {
        return new Tank(false, type, x, y, type.health, 0);
    }

    public Rectangle bounds() {
        return new Rectangle(x + 3f, y + 3f, GameConfig.TILE_SIZE - 6f, GameConfig.TILE_SIZE - 6f);
    }

    public float centerX() {
        return x + GameConfig.TILE_SIZE / 2f;
    }

    public float centerY() {
        return y + GameConfig.TILE_SIZE / 2f;
    }

    public float baseSpeed() {
        if (player) {
            return speedTimer > 0f ? GameConfig.PLAYER_SPEED_POWERUP : GameConfig.PLAYER_SPEED;
        }
        return enemyType.speed;
    }

    public boolean hasShield() {
        return shieldTimer > 0f;
    }

    public boolean hasPowerShot() {
        return powerShotTimer > 0f;
    }

    public String region() {
        return player ? AssetKeys.PLAYER : enemyType.region;
    }

    public boolean isPlayer() {
        return player;
    }

    public EnemyType enemyType() {
        return enemyType;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Direction direction() {
        return direction;
    }

    public void direction(Direction direction) {
        this.direction = direction;
    }

    public int health() {
        return health;
    }

    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            alive = false;
        }
    }

    public int lives() {
        return lives;
    }

    public void lives(int lives) {
        this.lives = lives;
    }

    public float fireCooldown() {
        return fireCooldown;
    }

    public void fireCooldown(float fireCooldown) {
        this.fireCooldown = Math.max(0f, fireCooldown);
    }

    public float aiDecisionTimer() {
        return aiDecisionTimer;
    }

    public void aiDecisionTimer(float aiDecisionTimer) {
        this.aiDecisionTimer = Math.max(0f, aiDecisionTimer);
    }

    public float shieldTimer() {
        return shieldTimer;
    }

    public void shieldTimer(float shieldTimer) {
        this.shieldTimer = Math.max(0f, shieldTimer);
    }

    public float speedTimer() {
        return speedTimer;
    }

    public void speedTimer(float speedTimer) {
        this.speedTimer = Math.max(0f, speedTimer);
    }

    public float powerShotTimer() {
        return powerShotTimer;
    }

    public void powerShotTimer(float powerShotTimer) {
        this.powerShotTimer = Math.max(0f, powerShotTimer);
    }

    public boolean alive() {
        return alive;
    }

    public void revive() {
        this.alive = true;
        this.health = player ? 1 : enemyType.health;
    }
}
