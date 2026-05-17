package com.silence.tank;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public final class LevelLoader {
    private LevelLoader() {
    }

    public static LevelDefinition load(FileHandle handle) {
        return load(handle.readString("UTF-8"));
    }

    public static LevelDefinition load(String jsonText) {
        JsonValue root = new JsonReader().parse(jsonText);
        String name = root.getString("name", "Unnamed");
        int width = root.getInt("width");
        int height = root.getInt("height");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Level dimensions must be positive.");
        }

        TileType[][] tiles = readTiles(root.require("tiles"), width, height);
        GridCoord playerSpawn = readCoord(root.require("player"));
        GridCoord base = readCoord(root.require("base"));
        validateCoord(playerSpawn, width, height, "player");
        validateCoord(base, width, height, "base");

        List<EnemyWave> waves = new ArrayList<>();
        JsonValue wavesNode = root.get("waves");
        if (wavesNode != null) {
            for (JsonValue wave = wavesNode.child; wave != null; wave = wave.next) {
                EnemyType type = EnemyType.valueOf(wave.getString("type").toUpperCase());
                int count = wave.getInt("count");
                float spawnEvery = wave.getFloat("spawnEvery", 1.5f);
                List<GridCoord> spawnPoints = new ArrayList<>();
                for (JsonValue point = wave.require("spawnPoints").child; point != null; point = point.next) {
                    GridCoord coord = readCoord(point);
                    validateCoord(coord, width, height, "spawnPoint");
                    spawnPoints.add(coord);
                }
                waves.add(new EnemyWave(type, count, spawnEvery, spawnPoints));
            }
        }

        List<PowerUpSpawn> powerUps = new ArrayList<>();
        JsonValue powerNode = root.get("powerUps");
        if (powerNode != null) {
            for (JsonValue power = powerNode.child; power != null; power = power.next) {
                PowerUpType type = PowerUpType.valueOf(power.getString("type").toUpperCase());
                GridCoord coord = readCoord(power);
                validateCoord(coord, width, height, "powerUp");
                powerUps.add(new PowerUpSpawn(type, coord));
            }
        }

        return new LevelDefinition(name, width, height, tiles, playerSpawn, base, waves, powerUps);
    }

    private static TileType[][] readTiles(JsonValue rows, int width, int height) {
        if (rows.size != height) {
            throw new IllegalArgumentException("Expected " + height + " tile rows, got " + rows.size + ".");
        }
        TileType[][] tiles = new TileType[height][width];
        int y = 0;
        for (JsonValue row = rows.child; row != null; row = row.next, y++) {
            String text = row.asString();
            if (text.length() != width) {
                throw new IllegalArgumentException("Row " + y + " expected width " + width + ", got " + text.length() + ".");
            }
            for (int x = 0; x < width; x++) {
                tiles[y][x] = TileType.fromCode(text.charAt(x));
            }
        }
        return tiles;
    }

    private static GridCoord readCoord(JsonValue value) {
        return new GridCoord(value.getInt("x"), value.getInt("y"));
    }

    private static void validateCoord(GridCoord coord, int width, int height, String label) {
        if (coord.x() < 0 || coord.x() >= width || coord.y() < 0 || coord.y() >= height) {
            throw new IllegalArgumentException(label + " is outside the level: " + coord);
        }
    }
}
