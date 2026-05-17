package com.silence.tank;

import com.badlogic.gdx.math.Rectangle;

public final class PowerUp {
    private final PowerUpType type;
    private final float x;
    private final float y;
    private float remainingSeconds;
    private boolean active = true;

    public PowerUp(PowerUpType type, float x, float y) {
        this(type, x, y, GameConfig.POWERUP_DESPAWN_SECONDS);
    }

    public PowerUp(PowerUpType type, float x, float y, float remainingSeconds) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.remainingSeconds = remainingSeconds;
    }

    public Rectangle bounds() {
        return new Rectangle(x + 4f, y + 4f, GameConfig.TILE_SIZE - 8f, GameConfig.TILE_SIZE - 8f);
    }

    public void update(float delta) {
        if (!active) {
            return;
        }
        remainingSeconds -= delta;
        if (remainingSeconds <= 0f) {
            active = false;
        }
    }

    public PowerUpType type() {
        return type;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public boolean active() {
        return active;
    }

    public float remainingSeconds() {
        return remainingSeconds;
    }

    public void consume() {
        active = false;
    }
}
