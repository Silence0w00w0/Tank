package com.silence.tank;

public final class GameConfig {
    public static final int TILE_SIZE = 32;
    public static final int MAP_WIDTH = 26;
    public static final int MAP_HEIGHT = 20;
    public static final int HUD_HEIGHT = 80;
    public static final int ARENA_WIDTH = MAP_WIDTH * TILE_SIZE;
    public static final int ARENA_HEIGHT = MAP_HEIGHT * TILE_SIZE;
    public static final int WINDOW_WIDTH = ARENA_WIDTH;
    public static final int WINDOW_HEIGHT = ARENA_HEIGHT + HUD_HEIGHT;

    public static final float PLAYER_SPEED = 112f;
    public static final float PLAYER_SPEED_POWERUP = 156f;
    public static final float PLAYER_FIRE_COOLDOWN = 0.36f;
    public static final float PLAYER_POWER_FIRE_COOLDOWN = 0.22f;
    public static final float ENEMY_FIRE_COOLDOWN = 1.05f;
    public static final float BULLET_SPEED = 288f;
    public static final float POWER_BULLET_SPEED = 360f;
    public static final float SHIELD_SECONDS = 8f;
    public static final float SPEED_SECONDS = 8f;
    public static final float POWER_SHOT_SECONDS = 10f;
    public static final float RESPAWN_SHIELD_SECONDS = 2f;
    public static final int INITIAL_LIVES = 3;
    public static final int MAX_ENEMIES_ON_FIELD = 5;
    public static final float LEVEL_ADVANCE_SECONDS = 1.7f;

    private GameConfig() {
    }
}
