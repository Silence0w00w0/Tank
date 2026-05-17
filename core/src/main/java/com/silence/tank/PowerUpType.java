package com.silence.tank;

public enum PowerUpType {
    SHIELD(AssetKeys.POWER_SHIELD),
    SPEED(AssetKeys.POWER_SPEED),
    POWER_SHOT(AssetKeys.POWER_SHOT),
    FORTIFY_BASE(AssetKeys.POWER_SHIELD),
    CLEAR_SCREEN(AssetKeys.POWER_BOMB),
    EXTRA_LIFE(AssetKeys.POWER_LIFE);

    public final String region;

    PowerUpType(String region) {
        this.region = region;
    }
}
