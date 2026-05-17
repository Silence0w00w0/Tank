package com.silence.tank;

import java.util.List;

public final class LevelDefinition {
    private final String name;
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final GridCoord playerSpawn;
    private final GridCoord basePosition;
    private final List<EnemyWave> waves;
    private final List<PowerUpSpawn> powerUps;

    public LevelDefinition(
            String name,
            int width,
            int height,
            TileType[][] tiles,
            GridCoord playerSpawn,
            GridCoord basePosition,
            List<EnemyWave> waves,
            List<PowerUpSpawn> powerUps
    ) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tiles = copyTiles(tiles, width, height);
        this.playerSpawn = playerSpawn;
        this.basePosition = basePosition;
        this.waves = List.copyOf(waves);
        this.powerUps = List.copyOf(powerUps);
    }

    public String name() {
        return name;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public TileType tileAt(int x, int y) {
        return tiles[y][x];
    }

    public TileType[][] copyTiles() {
        return copyTiles(tiles, width, height);
    }

    public GridCoord playerSpawn() {
        return playerSpawn;
    }

    public GridCoord basePosition() {
        return basePosition;
    }

    public List<EnemyWave> waves() {
        return waves;
    }

    public List<PowerUpSpawn> powerUps() {
        return powerUps;
    }

    private static TileType[][] copyTiles(TileType[][] source, int width, int height) {
        if (source.length != height) {
            throw new IllegalArgumentException("Tile row count does not match level height.");
        }
        TileType[][] copy = new TileType[height][width];
        for (int y = 0; y < height; y++) {
            if (source[y].length != width) {
                throw new IllegalArgumentException("Tile column count does not match level width.");
            }
            System.arraycopy(source[y], 0, copy[y], 0, width);
        }
        return copy;
    }
}
