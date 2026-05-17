package com.silence.tank;

import com.badlogic.gdx.math.Rectangle;

public final class PowerUp {
    private final PowerUpType type;
    private final float x;
    private final float y;
    private boolean active = true;

    public PowerUp(PowerUpType type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public Rectangle bounds() {
        return new Rectangle(x + 4f, y + 4f, GameConfig.TILE_SIZE - 8f, GameConfig.TILE_SIZE - 8f);
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

    public void consume() {
        active = false;
    }
}
