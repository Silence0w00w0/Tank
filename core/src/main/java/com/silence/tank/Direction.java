package com.silence.tank;

public enum Direction {
    UP(0, 1, 0f),
    RIGHT(1, 0, -90f),
    DOWN(0, -1, 180f),
    LEFT(-1, 0, 90f);

    public final int dx;
    public final int dy;
    public final float rotation;

    Direction(int dx, int dy, float rotation) {
        this.dx = dx;
        this.dy = dy;
        this.rotation = rotation;
    }

    public boolean isHorizontal() {
        return dx != 0;
    }
}
