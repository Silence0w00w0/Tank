package com.silence.tank;

public enum TileType {
    EMPTY('.', false, false, null),
    BRICK('B', true, true, AssetKeys.TILE_BRICK),
    STEEL('S', true, false, AssetKeys.TILE_STEEL),
    WATER('W', true, false, AssetKeys.TILE_WATER),
    GRASS('G', false, false, AssetKeys.TILE_GRASS),
    ICE('I', false, false, AssetKeys.TILE_ICE);

    public final char code;
    public final boolean blocksTank;
    public final boolean destructible;
    public final String region;

    TileType(char code, boolean blocksTank, boolean destructible, String region) {
        this.code = code;
        this.blocksTank = blocksTank;
        this.destructible = destructible;
        this.region = region;
    }

    public boolean blocksBullet(boolean powerShot) {
        return this == BRICK || this == STEEL;
    }

    public static TileType fromCode(char code) {
        for (TileType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tile code: " + code);
    }
}
