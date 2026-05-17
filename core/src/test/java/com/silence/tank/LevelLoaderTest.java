package com.silence.tank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelLoaderTest {
    @Test
    void parsesLevelTilesWavesAndPowerUps() {
        LevelDefinition level = LevelLoader.load("""
                {
                  "name": "Test",
                  "width": 4,
                  "height": 3,
                  "player": { "x": 1, "y": 2 },
                  "base": { "x": 2, "y": 2 },
                  "tiles": [
                    ".BSW",
                    "GII.",
                    "...."
                  ],
                  "waves": [
                    {
                      "type": "ARMORED",
                      "count": 2,
                      "spawnEvery": 1.5,
                      "spawnPoints": [{ "x": 0, "y": 0 }]
                    }
                  ],
                  "powerUps": [
                    { "type": "SHIELD", "x": 1, "y": 1 }
                  ]
                }
                """);

        assertEquals("Test", level.name());
        assertEquals(TileType.BRICK, level.tileAt(1, 0));
        assertEquals(TileType.GRASS, level.tileAt(0, 1));
        assertEquals(EnemyType.ARMORED, level.waves().get(0).type());
        assertEquals(PowerUpType.SHIELD, level.powerUps().get(0).type());
    }

    @Test
    void rejectsRowsWithWrongWidth() {
        assertThrows(IllegalArgumentException.class, () -> LevelLoader.load("""
                {
                  "width": 3,
                  "height": 1,
                  "player": { "x": 0, "y": 0 },
                  "base": { "x": 1, "y": 0 },
                  "tiles": ["...."]
                }
                """));
    }
}
