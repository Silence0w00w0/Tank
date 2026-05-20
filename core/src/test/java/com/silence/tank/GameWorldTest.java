package com.silence.tank;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
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
        world.setBaseShieldTimerForTest(0f);
        world.addBulletForTest(new Bullet(false, false, world.baseBounds().x + 12f, world.baseBounds().y + 12f, Direction.DOWN));

        world.update(0.01f, InputCommand.none());

        assertEquals(GameStatus.GAME_OVER, world.status());
        assertTrue(!world.baseAlive());
    }

    @Test
    void respawnShieldsPlayerAndBaseProtectionWalls() {
        GameWorld world = new GameWorld(List.of(protectedBaseLevel()), new Random(8));
        world.startNewGame();
        world.addEnemyForTest(Tank.enemy(EnemyType.ARMORED, world.tileToWorldX(3), world.tileToWorldY(0)));
        world.setBaseShieldTimerForTest(0f);
        world.player().shieldTimer(0f);
        world.addBulletForTest(new Bullet(false, false, world.player().x() + 12f, world.player().y() + 12f, Direction.DOWN));

        world.update(0.01f, InputCommand.none());

        assertEquals(2, world.player().lives());
        assertTrue(world.player().hasShield());
        assertTrue(world.baseShielded());

        GridCoord base = world.level().basePosition();
        int wallX = base.x();
        int wallY = base.y() - 1;
        world.addBulletForTest(new Bullet(true, false, world.tileToWorldX(wallX) + 12f, world.tileToWorldY(wallY) - 2f, Direction.UP));
        world.update(0.05f, InputCommand.none());

        assertTrue(world.baseAlive());
        assertEquals(GameStatus.PLAYING, world.status());
        assertEquals(TileType.BRICK, world.tileAt(wallX, wallY));
    }

    @Test
    void baseWallShieldDoesNotProtectBaseItself() {
        GameWorld world = new GameWorld(List.of(protectedBaseLevel()), new Random(8));
        world.startNewGame();
        assertTrue(world.baseShielded());

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
    void fortifyBasePowerUpConvertsProtectionWallsToSteel() {
        GameWorld world = new GameWorld(List.of(protectedBaseLevel()), new Random(8));
        world.startNewGame();
        world.addPowerUpForTest(new PowerUp(PowerUpType.FORTIFY_BASE, world.player().x(), world.player().y()));

        world.update(0.01f, InputCommand.none());

        GridCoord base = world.level().basePosition();
        assertEquals(TileType.STEEL, world.tileAt(base.x() - 1, base.y() - 1));
        assertEquals(TileType.STEEL, world.tileAt(base.x(), base.y() - 1));
        assertEquals(TileType.STEEL, world.tileAt(base.x() + 1, base.y() - 1));
        assertEquals(TileType.STEEL, world.tileAt(base.x() - 1, base.y()));
        assertEquals(TileType.STEEL, world.tileAt(base.x() + 1, base.y()));
    }

    @Test
    void higherLevelsUnlockMorePowerUpTypes() {
        LevelDefinition level = protectedBaseLevel();
        GameWorld world = new GameWorld(List.of(level, level, level), new LastDropRandom());
        world.loadLevel(2, GameConfig.INITIAL_LIVES);
        Tank enemy = Tank.enemy(EnemyType.BASIC, world.tileToWorldX(3), world.tileToWorldY(1));
        world.addEnemyForTest(enemy);
        world.addBulletForTest(new Bullet(true, false, enemy.x() + 12f, enemy.y() + 12f, Direction.UP));

        world.update(0f, InputCommand.none());

        assertEquals(1, world.powerUps().size());
        assertEquals(PowerUpType.EXTRA_LIFE, world.powerUps().get(0).type());
    }

    @Test
    void enemyPowerTankBulletUsesNormalSpeed() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addEnemyForTest(Tank.enemy(EnemyType.POWER, world.tileToWorldX(1), world.tileToWorldY(0)));

        world.update(0.01f, InputCommand.none());

        Bullet enemyBullet = world.bullets().stream().filter(bullet -> !bullet.fromPlayer()).findFirst().orElseThrow();
        assertEquals(GameConfig.BULLET_SPEED, enemyBullet.speed(), 0.001f);
        assertTrue(enemyBullet.powerShot());
    }

    @Test
    void levelClearRequiresEnterToAdvance() {
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
        for (int i = 0; i < 100; i++) {
            world.update(0.1f, InputCommand.none());
        }
        world.update(0.01f, InputCommand.none().fire(true));

        assertEquals(0, world.levelIndex());
        assertEquals(GameStatus.LEVEL_CLEAR, world.status());

        world.update(0.01f, InputCommand.none().start(true));

        assertEquals(1, world.levelIndex());
        assertEquals(GameStatus.PLAYING, world.status());
    }

    @Test
    void autoCommandFiresAtVisibleEnemy() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addEnemyForTest(Tank.enemy(EnemyType.BASIC, world.tileToWorldX(1), world.tileToWorldY(2)));

        InputCommand command = world.autoCommandForPlayer(0);

        assertEquals(Direction.UP, command.moveDirection());
        assertTrue(command.fire());
    }

    @Test
    void autoCommandTurnsWhenBulletLaneOverlapsAcrossGridRows() {
        GameWorld world = worldWithRows(
                ".....",
                ".....",
                ".....",
                "....."
        );
        world.player().setPosition(world.tileToWorldX(1), world.tileToWorldY(3) + 15.5f);
        world.addEnemyForTest(Tank.enemy(EnemyType.BASIC, world.tileToWorldX(3), world.tileToWorldY(2)));

        InputCommand command = world.autoCommandForPlayer(0);

        assertEquals(Direction.RIGHT, command.moveDirection());
        assertTrue(command.fire());
    }

    @Test
    void autoCommandDoesNotFireThroughBase() {
        GameWorld world = new GameWorld(List.of(LevelLoader.load("""
                {
                  "name": "Auto Safety",
                  "width": 3,
                  "height": 4,
                  "player": { "x": 1, "y": 1 },
                  "base": { "x": 1, "y": 2 },
                  "tiles": [
                    "...",
                    "...",
                    "...",
                    "..."
                  ]
                }
                """)), new Random(4));
        world.startNewGame();
        world.addEnemyForTest(Tank.enemy(EnemyType.BASIC, world.tileToWorldX(1), world.tileToWorldY(3)));

        InputCommand command = world.autoCommandForPlayer(0);

        assertTrue(!command.fire());
    }

    @Test
    void autoCommandMovesTowardPowerUp() {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addPowerUpForTest(new PowerUp(PowerUpType.SHIELD, world.tileToWorldX(1), world.tileToWorldY(2)));

        InputCommand command = world.autoCommandForPlayer(0);

        assertEquals(Direction.UP, command.moveDirection());
        assertTrue(!command.fire());
    }

    @Test
    void autoCommandPatrolsForwardWhenNoEnemyIsVisible() {
        GameWorld world = new GameWorld(List.of(protectedBaseLevel()), new Random(8));
        world.startNewGame();

        InputCommand command = world.autoCommandForPlayer(0);

        assertEquals(Direction.UP, command.moveDirection());
        assertTrue(!command.fire());
    }

    @Test
    void autoCommandDoesNotIdleOnCurrentDefensePoint() {
        GameWorld world = new GameWorld(List.of(LevelLoader.load("""
                {
                  "name": "Auto Defense Patrol",
                  "width": 5,
                  "height": 4,
                  "player": { "x": 0, "y": 1 },
                  "base": { "x": 2, "y": 3 },
                  "tiles": [
                    ".....",
                    ".....",
                    ".BBB.",
                    ".B.B."
                  ]
                }
                """)), new Random(8));
        world.startNewGame();

        InputCommand command = world.autoCommandForPlayer(0);

        assertTrue(command.moveDirection() != null);
        assertTrue(!command.fire());
    }

    @Test
    void autoCommandCanUseExistingInputSerialization() throws Exception {
        GameWorld world = worldWithRows(
                "....",
                "....",
                "....",
                "...."
        );
        world.addEnemyForTest(Tank.enemy(EnemyType.BASIC, world.tileToWorldX(1), world.tileToWorldY(2)));
        InputCommand command = world.autoCommandForPlayer(0);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(command);
        }

        assertTrue(bytes.size() > 0);
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

    private static LevelDefinition protectedBaseLevel() {
        return LevelLoader.load("""
                {
                  "name": "Protected Base",
                  "width": 5,
                  "height": 4,
                  "player": { "x": 0, "y": 3 },
                  "base": { "x": 2, "y": 3 },
                  "tiles": [
                    ".....",
                    ".....",
                    ".BBB.",
                    ".B.B."
                  ]
                }
                """);
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

    private static final class LastDropRandom extends Random {
        @Override
        public float nextFloat() {
            return 0f;
        }

        @Override
        public int nextInt(int bound) {
            return bound - 1;
        }
    }
}
