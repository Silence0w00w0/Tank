package com.silence.tank;

import com.badlogic.gdx.math.Rectangle;

public final class Bullet {
    private final boolean fromPlayer;
    private final boolean powerShot;
    private float x;
    private float y;
    private final Direction direction;
    private boolean alive = true;

    public Bullet(boolean fromPlayer, boolean powerShot, float x, float y, Direction direction) {
        this.fromPlayer = fromPlayer;
        this.powerShot = powerShot;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public void update(float delta) {
        float speed = powerShot ? GameConfig.POWER_BULLET_SPEED : GameConfig.BULLET_SPEED;
        x += direction.dx * speed * delta;
        y += direction.dy * speed * delta;
    }

    public Rectangle bounds() {
        return new Rectangle(x, y, 8f, 8f);
    }

    public boolean fromPlayer() {
        return fromPlayer;
    }

    public boolean powerShot() {
        return powerShot;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public Direction direction() {
        return direction;
    }

    public boolean alive() {
        return alive;
    }

    public void destroy() {
        alive = false;
    }
}
