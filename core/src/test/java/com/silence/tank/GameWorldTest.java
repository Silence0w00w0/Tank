package com.silence.tank;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameWorldTest {
    @Test
    void tankMovementIsBlockedBySolidTiles() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "..B."
        );
        float startX = world.player().x();

        world.update(0.1f, InputCommand.none().move(Direction.RIGHT));

        assertEquals(startX, world.player().x(), 0.001f);
    }

    @Test
    void playerBulletDestroysBrickTile() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "..B.",
                "...."
        );
        world.addBulletForTest(new Bullet(true, false, world.tileToWorldX(2) + 12f, world.tileToWorldY(2) - 2f, Direction.UP));

        world.update(0.05f, InputCommand.none());

        assertEquals(TileType.EMPTY, world.tileAt(2, 2));
    }

    @Test
    void powerShotDestroysSteelTile() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "..S.",
                "...."
        );
        world.addBulletForTest(new Bullet(true, true, world.tileToWorldX(2) + 12f, world.tileToWorldY(2) - 2f, Direction.UP));

        world.update(0.05f, InputCommand.none());

        assertEquals(TileType.EMPTY, world.tileAt(2, 2));
    }

    @Test
    void enemyBulletDestroysBaseAndEndsGame() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addBulletForTest(new Bullet(false, false, world.baseBounds().x + 12f, world.baseBounds().y + 12f, Direction.DOWN));

        world.update(0.01f, InputCommand.none());

        assertEquals(GameStatus.GAME_OVER, world.status());
        assertTrue(!world.baseAlive());
    }

    @Test
    void shieldPowerUpPreventsLifeLoss() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addPowerUpForTest(new PowerUp(PowerUpType.SHIELD, world.player().x(), world.player().y()));
        world.update(0.01f, InputCommand.none());
        int lives = world.player().lives();

        world.addBulletForTest(new Bullet(false, false, world.player().x() + 12f, world.player().y() + 12f, Direction.DOWN));
        world.update(0.01f, InputCommand.none());

        assertEquals(lives, world.player().lives());
        assertTrue(world.player().hasShield());
    }

    @Test
    void killedEnemyCanDropRandomPowerUp() {
        GameWorld world = new GameWorld(List.of(levelWithRows("Drop",
                "....",
                "....",
                "....",
                "...."
        )), new AlwaysDropRandom());
        world.startNewGame();
        Tank enemy = Tank.enemy(EnemyType.BASIC, world.tileToWorldX(2), world.tileToWorldY(2));
        world.addEnemyForTest(enemy);
        world.addBulletForTest(new Bullet(true, false, enemy.x() + 12f, enemy.y() + 12f, Direction.UP));

        world.update(0f, InputCommand.none());

        assertTrue(world.enemies().isEmpty());
        assertEquals(1, world.powerUps().size());
        assertEquals(PowerUpType.SHIELD, world.powerUps().get(0).type());
    }

    @Test
    void droppedPowerUpExpiresAfterTimer() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addPowerUpForTest(new PowerUp(PowerUpType.SPEED, world.tileToWorldX(3), world.tileToWorldY(1), 0.01f));

        world.update(0.02f, InputCommand.none());

        assertTrue(world.powerUps().isEmpty());
    }

    @Test
    void playerAndEnemyBulletsCancelEachOther() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        float x = world.tileToWorldX(3) + 8f;
        float y = world.tileToWorldY(1) + 8f;
        world.addBulletForTest(new Bullet(true, false, x, y, Direction.UP));
        world.addBulletForTest(new Bullet(false, false, x, y, Direction.DOWN));

        world.update(0f, InputCommand.none());

        assertTrue(world.bullets().isEmpty());
    }

    @Test
    void fastOpposingBulletsCancelWhenPathsCross() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        float y = world.tileToWorldY(1) + 8f;
        world.addBulletForTest(new Bullet(true, false, world.tileToWorldX(2), y, Direction.RIGHT));
        world.addBulletForTest(new Bullet(false, false, world.tileToWorldX(2) + 18f, y, Direction.LEFT));

        world.update(0.05f, InputCommand.none());

        assertTrue(world.bullets().isEmpty());
    }

    @Test
    void twoPlayerWorldKeepsSeparateLivesAndSnapshotState() {
        LevelDefinition level = LevelLoader.load("""
                {
                  "name": "Two Player",
                  "width": 5,
                  "height": 4,
                  "player": { "x": 1, "y": 3 },
                  "players": [
                    { "x": 1, "y": 3 },
                    { "x": 3, "y": 3 }
                  ],
                  "base": { "x": 2, "y": 2 },
                  "tiles": [
                    ".....",
                    ".BBB.",
                    ".B.B.",
                    ".BBB."
                  ]
                }
                """);
        GameWorld hostWorld = new GameWorld(List.of(level), new Random(4), 2);
        hostWorld.startNewGame();
        hostWorld.update(0.1f, List.of(InputCommand.none(), InputCommand.none().move(Direction.LEFT)));

        GameSnapshot snapshot = GameSnapshot.from(hostWorld);
        GameWorld clientWorld = new GameWorld(List.of(level), new Random(5), 2);
        clientWorld.applySnapshot(snapshot);

        assertEquals(2, clientWorld.players().size());
        assertEquals(hostWorld.players().get(1).x(), clientWorld.players().get(1).x(), 0.001f);
        assertEquals(TileType.BRICK, clientWorld.tileAt(1, 1));
    }

    @Test
    void emptyLevelsAdvanceToVictory() {
        LevelDefinition one = levelWithRows("One",
                "....",
                "....",
                "....",
                "...."
        );
        LevelDefinition two = levelWithRows("Two",
                "....",
                "....",
                "....",
                "...."
        );
        GameWorld world = new GameWorld(List.of(one, two), new Random(3));
        world.startNewGame();

        world.update(0.1f, InputCommand.none());
        assertEquals(GameStatus.LEVEL_CLEAR, world.status());
        world.update(0.01f, InputCommand.none().start(true));
        world.update(0.1f, InputCommand.none());
        assertEquals(GameStatus.LEVEL_CLEAR, world.status());
        world.update(0.01f, InputCommand.none().start(true));

        assertEquals(GameStatus.VICTORY, world.status());
    }

    private static GameWorld worldWithRows(String... rows) {
        GameWorld world = new GameWorld(List.of(levelWithRows("Test", rows)), new Random(7));
        world.startNewGame();
        return world;
    }

    private static LevelDefinition levelWithRows(String name, String... rows) {
        StringBuilder tiles = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            if (i > 0) {
                tiles.append(",\n");
            }
            tiles.append("    \"").append(rows[i]).append("\"");
        }
        return LevelLoader.load("""
                {
                  "name": "%s",
                  "width": %d,
                  "height": %d,
                  "player": { "x": 1, "y": %d },
                  "base": { "x": 0, "y": %d },
                  "tiles": [
                %s
                  ]
                }
                """.formatted(name, rows[0].length(), rows.length, rows.length - 1, rows.length - 1, tiles));
    }

    private static final class AlwaysDropRandom extends Random {
        @Override
        public float nextFloat() {
            return 0f;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }
}
