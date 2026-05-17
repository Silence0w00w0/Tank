package com.silence.tank;

public final class Explosion {
    private final float x;
    private final float y;
    private float timer;

    public Explosion(float x, float y) {
        this(x, y, 0.32f);
    }

    public Explosion(float x, float y, float timer) {
        this.x = x;
        this.y = y;
        this.timer = timer;
    }

    public void update(float delta) {
        timer -= delta;
    }

    public String region() {
        if (timer > 0.21f) {
            return AssetKeys.EXPLOSION_1;
        }
        if (timer > 0.1f) {
            return AssetKeys.EXPLOSION_2;
        }
        return AssetKeys.EXPLOSION_3;
    }

    public boolean alive() {
        return timer > 0f;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float timer() {
        return timer;
    }
}
