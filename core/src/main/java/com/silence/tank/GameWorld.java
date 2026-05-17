package com.silence.tank;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class GameWorld {
    private final List<LevelDefinition> levels;
    private final Random random;
    private final int playerCount;
    private final List<Tank> players = new ArrayList<>();
    private final List<Tank> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<WaveRuntime> waves = new ArrayList<>();

    private LevelDefinition level;
    private TileType[][] tiles;
    private GameStatus status = GameStatus.MENU;
    private int levelIndex;
    private int score;
    private int highScore;
    private boolean baseAlive = true;
    private float baseShieldTimer;
    private float levelElapsedSeconds;

    public GameWorld(List<LevelDefinition> levels, Random random) {
        this(levels, random, 1);
    }

    public GameWorld(List<LevelDefinition> levels, Random random, int playerCount) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("At least one level is required.");
        }
        if (playerCount < 1 || playerCount > 2) {
            throw new IllegalArgumentException("Supported player count is 1 or 2.");
        }
        this.levels = List.copyOf(levels);
        this.random = random;
        this.playerCount = playerCount;
        startNewGame();
        status = GameStatus.MENU;
    }

    public void update(float delta, InputCommand command) {
        update(delta, List.of(command));
    }

    public void update(float delta, List<InputCommand> commands) {
        delta = Math.min(delta, 1f / 20f);
        if (anyRestart(commands)) {
            startNewGame();
            return;
        }
        if (status == GameStatus.MENU) {
            if (anyStartOrFire(commands)) {
                status = GameStatus.PLAYING;
            }
            return;
        }
        if (status == GameStatus.PAUSED) {
            if (anyPause(commands)) {
                status = GameStatus.PLAYING;
            }
            return;
        }
        if (status == GameStatus.GAME_OVER || status == GameStatus.VICTORY) {
            if (anyStartOrFire(commands)) {
                startNewGame();
            }
            return;
        }
        if (status == GameStatus.LEVEL_CLEAR) {
            if (anyStart(commands)) {
                advanceLevel();
            }
            return;
        }
        if (anyPause(commands)) {
            status = GameStatus.PAUSED;
            return;
        }

        levelElapsedSeconds += delta;
        tickBaseShield(delta);
        tickPlayers(delta, commands);
        tickEnemies(delta);
        spawnEnemies(delta);
        tickBullets(delta);
        tickPowerUps(delta);
        collectPowerUps();
        tickExplosions(delta);
        checkLevelComplete();
        highScore = Math.max(highScore, score);
    }

    public void startNewGame() {
        score = 0;
        levelIndex = 0;
        loadLevel(levelIndex, GameConfig.INITIAL_LIVES);
        status = GameStatus.PLAYING;
    }

    public void loadLevel(int index, int lives) {
        int[] playerLives = new int[playerCount];
        for (int i = 0; i < playerLives.length; i++) {
            playerLives[i] = lives;
        }
        loadLevel(index, playerLives);
    }

    public void loadLevel(int index, int[] playerLives) {
        levelIndex = index;
        level = levels.get(index);
        tiles = level.copyTiles();
        baseAlive = true;
        baseShieldTimer = GameConfig.BASE_RESPAWN_SHIELD_SECONDS;
        levelElapsedSeconds = 0f;
        players.clear();
        enemies.clear();
        bullets.clear();
        powerUps.clear();
        explosions.clear();
        waves.clear();
        for (int i = 0; i < playerCount; i++) {
            GridCoord spawn = level.playerSpawn(i);
            int lives = i < playerLives.length ? playerLives[i] : GameConfig.INITIAL_LIVES;
            Tank player = Tank.player(tileToWorldX(spawn.x()), tileToWorldY(spawn.y()), lives);
            player.direction(Direction.UP);
            player.shieldTimer(GameConfig.RESPAWN_SHIELD_SECONDS);
            players.add(player);
        }
        for (EnemyWave wave : level.waves()) {
            waves.add(new WaveRuntime(wave));
        }
        status = GameStatus.PLAYING;
    }

    private void advanceLevel() {
        if (levelIndex + 1 >= levels.size()) {
            status = GameStatus.VICTORY;
            highScore = Math.max(highScore, score);
            return;
        }
        loadLevel(levelIndex + 1, playerLives());
    }

    private void tickPlayers(float delta, List<InputCommand> commands) {
        for (int i = 0; i < players.size(); i++) {
            tickPlayer(players.get(i), delta, commandAt(commands, i));
        }
    }

    private void tickPlayer(Tank player, float delta, InputCommand command) {
        if (!player.alive()) {
            return;
        }
        tickTankTimers(player, delta);
        if (command.moveDirection() != null) {
            moveTank(player, command.moveDirection(), delta);
        }
        if (command.fire()) {
            fire(player);
        }
    }

    private int[] playerLives() {
        int[] lives = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            lives[i] = players.get(i).lives();
        }
        return lives;
    }

    private InputCommand commandAt(List<InputCommand> commands, int index) {
        if (commands == null || index >= commands.size() || commands.get(index) == null) {
            return InputCommand.none();
        }
        return commands.get(index);
    }

    private boolean anyRestart(List<InputCommand> commands) {
        return commands != null && commands.stream().anyMatch(InputCommand::restart);
    }

    private boolean anyPause(List<InputCommand> commands) {
        return commands != null && commands.stream().anyMatch(InputCommand::pause);
    }

    private boolean anyStartOrFire(List<InputCommand> commands) {
        return commands != null && commands.stream().anyMatch(command -> command.start() || command.fire());
    }

    private boolean anyStart(List<InputCommand> commands) {
        return commands != null && commands.stream().anyMatch(InputCommand::start);
    }

    private void tickEnemies(float delta) {
        for (Tank enemy : enemies) {
            tickTankTimers(enemy, delta);
            Direction desired = chooseEnemyDirection(enemy, delta);
            moveTank(enemy, desired, delta);
            if (enemy.fireCooldown() <= 0f && canShootImportantTarget(enemy)) {
                fire(enemy);
            }
        }
    }

    private void tickTankTimers(Tank tank, float delta) {
        tank.fireCooldown(tank.fireCooldown() - delta);
        tank.aiDecisionTimer(tank.aiDecisionTimer() - delta);
        tank.shieldTimer(tank.shieldTimer() - delta);
        tank.speedTimer(tank.speedTimer() - delta);
        tank.powerShotTimer(tank.powerShotTimer() - delta);
    }

    private Direction chooseEnemyDirection(Tank enemy, float delta) {
        Direction targetDirection = directionToVisibleTarget(enemy);
        if (targetDirection != null) {
            enemy.direction(targetDirection);
            return targetDirection;
        }

        if (enemy.aiDecisionTimer() <= 0f) {
            Direction[] directions = Direction.values();
            Direction next = directions[random.nextInt(directions.length)];
            if (random.nextFloat() < 0.35f) {
                next = directionTowardBase(enemy);
            }
            enemy.direction(next);
            enemy.aiDecisionTimer(0.45f + random.nextFloat() * 1.2f);
        }
        return enemy.direction();
    }

    private Direction directionToVisibleTarget(Tank enemy) {
        for (Tank player : players) {
            if (!player.alive()) {
                continue;
            }
            Direction toPlayer = directionIfVisible(enemy, player.bounds());
            if (toPlayer != null) {
                return toPlayer;
            }
        }
        if (baseAlive) {
            return directionIfVisible(enemy, baseBounds());
        }
        return null;
    }

    private Direction directionIfVisible(Tank enemy, Rectangle target) {
        int enemyGridX = worldToGridX(enemy.centerX());
        int enemyGridY = worldToGridY(enemy.centerY());
        int targetGridX = worldToGridX(target.x + target.width / 2f);
        int targetGridY = worldToGridY(target.y + target.height / 2f);
        if (enemyGridX == targetGridX && clearLine(enemyGridX, enemyGridY, targetGridX, targetGridY)) {
            return targetGridY < enemyGridY ? Direction.UP : Direction.DOWN;
        }
        if (enemyGridY == targetGridY && clearLine(enemyGridX, enemyGridY, targetGridX, targetGridY)) {
            return targetGridX > enemyGridX ? Direction.RIGHT : Direction.LEFT;
        }
        return null;
    }

    private boolean clearLine(int x1, int y1, int x2, int y2) {
        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        int x = x1 + dx;
        int y = y1 + dy;
        while (x != x2 || y != y2) {
            TileType tile = tiles[y][x];
            if (tile == TileType.BRICK || tile == TileType.STEEL || tile == TileType.WATER) {
                return false;
            }
            x += dx;
            y += dy;
        }
        return true;
    }

    private Direction directionTowardBase(Tank enemy) {
        GridCoord base = level.basePosition();
        int ex = worldToGridX(enemy.centerX());
        int ey = worldToGridY(enemy.centerY());
        if (Math.abs(base.x() - ex) > Math.abs(base.y() - ey)) {
            return base.x() > ex ? Direction.RIGHT : Direction.LEFT;
        }
        return base.y() < ey ? Direction.UP : Direction.DOWN;
    }

    private boolean canShootImportantTarget(Tank enemy) {
        return directionToVisibleTarget(enemy) != null || random.nextFloat() < 0.01f;
    }

    private void moveTank(Tank tank, Direction requested, float delta) {
        if (requested == null) {
            return;
        }
        tank.direction(requested);
        alignToGrid(tank, requested);
        float speed = adjustedSpeed(tank);
        float nextX = tank.x() + requested.dx * speed * delta;
        float nextY = tank.y() + requested.dy * speed * delta;
        if (canOccupy(tank, nextX, nextY)) {
            tank.setPosition(nextX, nextY);
        } else if (!tank.isPlayer()) {
            tank.aiDecisionTimer(0f);
        }
    }

    private float adjustedSpeed(Tank tank) {
        float speed = tank.baseSpeed();
        if (tileUnder(tank) == TileType.ICE) {
            speed *= 1.18f;
        }
        return speed;
    }

    private void alignToGrid(Tank tank, Direction requested) {
        if (requested.isHorizontal()) {
            float snapped = Math.round(tank.y() / GameConfig.TILE_SIZE) * GameConfig.TILE_SIZE;
            if (Math.abs(snapped - tank.y()) < 3.2f) {
                tank.setPosition(tank.x(), snapped);
            }
        } else {
            float snapped = Math.round(tank.x() / GameConfig.TILE_SIZE) * GameConfig.TILE_SIZE;
            if (Math.abs(snapped - tank.x()) < 3.2f) {
                tank.setPosition(snapped, tank.y());
            }
        }
    }

    private boolean canOccupy(Tank mover, float x, float y) {
        Rectangle bounds = new Rectangle(x + 3f, y + 3f, GameConfig.TILE_SIZE - 6f, GameConfig.TILE_SIZE - 6f);
        if (bounds.x < 0f || bounds.y < 0f || bounds.x + bounds.width > arenaWidth() || bounds.y + bounds.height > arenaHeight()) {
            return false;
        }
        for (GridCoord coord : overlappingTiles(bounds)) {
            if (tiles[coord.y()][coord.x()].blocksTank) {
                return false;
            }
        }
        if (baseAlive && bounds.overlaps(baseBounds())) {
            return false;
        }
        for (Tank enemy : enemies) {
            if (enemy != mover && enemy.alive() && bounds.overlaps(enemy.bounds())) {
                return false;
            }
        }
        for (Tank player : players) {
            if (player != mover && player.alive() && bounds.overlaps(player.bounds())) {
                return false;
            }
        }
        return true;
    }

    private TileType tileUnder(Tank tank) {
        int gx = worldToGridX(tank.centerX());
        int gy = worldToGridY(tank.centerY());
        if (!inBounds(gx, gy)) {
            return TileType.EMPTY;
        }
        return tiles[gy][gx];
    }

    private void fire(Tank tank) {
        boolean powerShot = tank.isPlayer() ? tank.hasPowerShot() : tank.enemyType().powerShot;
        float bulletSpeed = tank.isPlayer() && powerShot ? GameConfig.POWER_BULLET_SPEED : GameConfig.BULLET_SPEED;
        float cooldown = tank.isPlayer()
                ? (powerShot ? GameConfig.PLAYER_POWER_FIRE_COOLDOWN : GameConfig.PLAYER_FIRE_COOLDOWN)
                : GameConfig.ENEMY_FIRE_COOLDOWN;
        if (tank.fireCooldown() > 0f) {
            return;
        }
        float bulletX = tank.centerX() - 4f + tank.direction().dx * 15f;
        float bulletY = tank.centerY() - 4f + tank.direction().dy * 15f;
        bullets.add(new Bullet(tank.isPlayer(), powerShot, bulletX, bulletY, tank.direction(), bulletSpeed));
        tank.fireCooldown(cooldown);
    }

    private void tickBullets(float delta) {
        for (Bullet bullet : bullets) {
            bullet.update(delta);
            if (bullet.x() < -8f || bullet.y() < -8f || bullet.x() > arenaWidth() || bullet.y() > arenaHeight()) {
                bullet.destroy();
            }
        }
        resolveBulletCollisions();
        for (Bullet bullet : bullets) {
            if (!bullet.alive()) {
                continue;
            }
            resolveBulletTileHit(bullet);
            if (bullet.alive()) {
                resolveBulletEntityHit(bullet);
            }
        }
        bullets.removeIf(bullet -> !bullet.alive());
    }

    private void resolveBulletCollisions() {
        for (int i = 0; i < bullets.size(); i++) {
            Bullet first = bullets.get(i);
            if (!first.alive()) {
                continue;
            }
            for (int j = i + 1; j < bullets.size(); j++) {
                Bullet second = bullets.get(j);
                if (!second.alive() || first.fromPlayer() == second.fromPlayer()) {
                    continue;
                }
                if (first.sweptBounds().overlaps(second.sweptBounds())) {
                    first.destroy();
                    second.destroy();
                    explosions.add(new Explosion(first.x() - 12f, first.y() - 12f));
                    break;
                }
            }
        }
    }

    private void resolveBulletTileHit(Bullet bullet) {
        for (GridCoord coord : overlappingTiles(bullet.bounds())) {
            TileType tile = tiles[coord.y()][coord.x()];
            if (tile.blocksBullet(bullet.powerShot())) {
                if (baseShielded() && isBaseProtectionWall(coord)) {
                    bullet.destroy();
                    explosions.add(new Explosion(tileToWorldX(coord.x()), tileToWorldY(coord.y())));
                    return;
                }
                if (tile == TileType.BRICK || (tile == TileType.STEEL && bullet.powerShot())) {
                    tiles[coord.y()][coord.x()] = TileType.EMPTY;
                    explosions.add(new Explosion(tileToWorldX(coord.x()), tileToWorldY(coord.y())));
                }
                bullet.destroy();
                return;
            }
        }
    }

    private void resolveBulletEntityHit(Bullet bullet) {
        if (baseAlive && bullet.bounds().overlaps(baseBounds())) {
            bullet.destroy();
            baseAlive = false;
            explosions.add(new Explosion(baseBounds().x, baseBounds().y));
            status = GameStatus.GAME_OVER;
            highScore = Math.max(highScore, score);
            return;
        }

        if (bullet.fromPlayer()) {
            Iterator<Tank> iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Tank enemy = iterator.next();
                if (bullet.bounds().overlaps(enemy.bounds())) {
                    enemy.damage(bullet.powerShot() ? 2 : 1);
                    bullet.destroy();
                    explosions.add(new Explosion(enemy.x(), enemy.y()));
                    if (!enemy.alive()) {
                        score += enemy.enemyType().score;
                        maybeDropPowerUp(enemy);
                        iterator.remove();
                    }
                    return;
                }
            }
        } else {
            for (Tank player : players) {
                if (player.alive() && bullet.bounds().overlaps(player.bounds())) {
                    bullet.destroy();
                    damagePlayer(player);
                    return;
                }
            }
        }
    }

    private void maybeDropPowerUp(Tank enemy) {
        if (random.nextFloat() >= currentPowerUpDropChance()) {
            return;
        }
        PowerUpType[] types = availablePowerUpsForCurrentLevel();
        PowerUpType type = types[random.nextInt(types.length)];
        float x = Math.max(0f, Math.min(enemy.x(), arenaWidth() - GameConfig.TILE_SIZE));
        float y = Math.max(0f, Math.min(enemy.y(), arenaHeight() - GameConfig.TILE_SIZE));
        powerUps.add(new PowerUp(type, x, y));
    }

    private float currentPowerUpDropChance() {
        return Math.min(
                GameConfig.POWERUP_MAX_DROP_CHANCE,
                GameConfig.POWERUP_BASE_DROP_CHANCE + levelIndex * GameConfig.POWERUP_DROP_CHANCE_PER_LEVEL
        );
    }

    private PowerUpType[] availablePowerUpsForCurrentLevel() {
        if (levelIndex <= 0) {
            return new PowerUpType[]{PowerUpType.SHIELD, PowerUpType.SPEED, PowerUpType.POWER_SHOT};
        }
        if (levelIndex == 1) {
            return new PowerUpType[]{PowerUpType.SHIELD, PowerUpType.SPEED, PowerUpType.POWER_SHOT, PowerUpType.FORTIFY_BASE, PowerUpType.CLEAR_SCREEN};
        }
        return PowerUpType.values();
    }

    private void tickPowerUps(float delta) {
        for (PowerUp powerUp : powerUps) {
            powerUp.update(delta);
        }
        powerUps.removeIf(powerUp -> !powerUp.active());
    }

    private void damagePlayer(Tank player) {
        if (player.hasShield()) {
            explosions.add(new Explosion(player.x(), player.y()));
            return;
        }
        int lives = player.lives() - 1;
        player.lives(lives);
        explosions.add(new Explosion(player.x(), player.y()));
        if (lives <= 0) {
            player.eliminate();
            if (players.stream().noneMatch(Tank::alive)) {
                status = GameStatus.GAME_OVER;
                highScore = Math.max(highScore, score);
            }
            return;
        }
        player.revive();
        int playerIndex = players.indexOf(player);
        GridCoord spawn = level.playerSpawn(Math.max(0, playerIndex));
        player.setPosition(tileToWorldX(spawn.x()), tileToWorldY(spawn.y()));
        player.direction(Direction.UP);
        player.shieldTimer(GameConfig.RESPAWN_SHIELD_SECONDS);
        baseShieldTimer = GameConfig.BASE_RESPAWN_SHIELD_SECONDS;
    }

    private void collectPowerUps() {
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.active()) {
                continue;
            }
            for (Tank player : players) {
                if (player.alive() && player.bounds().overlaps(powerUp.bounds())) {
                    applyPowerUp(player, powerUp.type());
                    powerUp.consume();
                    score += 50;
                    break;
                }
            }
        }
    }

    private void applyPowerUp(Tank player, PowerUpType type) {
        switch (type) {
            case SHIELD -> player.shieldTimer(GameConfig.SHIELD_SECONDS);
            case SPEED -> player.speedTimer(GameConfig.SPEED_SECONDS);
            case POWER_SHOT -> player.powerShotTimer(GameConfig.POWER_SHOT_SECONDS);
            case FORTIFY_BASE -> fortifyBaseWalls();
            case EXTRA_LIFE -> player.lives(player.lives() + 1);
            case CLEAR_SCREEN -> {
                for (Tank enemy : enemies) {
                    score += Math.max(50, enemy.enemyType().score / 2);
                    explosions.add(new Explosion(enemy.x(), enemy.y()));
                }
                enemies.clear();
                bullets.removeIf(bullet -> !bullet.fromPlayer());
            }
        }
    }

    private void fortifyBaseWalls() {
        GridCoord base = level.basePosition();
        for (GridCoord coord : baseProtectionCoords()) {
            if (coord.x() == base.x() && coord.y() == base.y()) {
                continue;
            }
            if (inBounds(coord.x(), coord.y())) {
                tiles[coord.y()][coord.x()] = TileType.STEEL;
                explosions.add(new Explosion(tileToWorldX(coord.x()), tileToWorldY(coord.y())));
            }
        }
    }

    private List<GridCoord> baseProtectionCoords() {
        GridCoord base = level.basePosition();
        return List.of(
                new GridCoord(base.x() - 1, base.y() - 1),
                new GridCoord(base.x(), base.y() - 1),
                new GridCoord(base.x() + 1, base.y() - 1),
                new GridCoord(base.x() - 1, base.y()),
                new GridCoord(base.x() + 1, base.y())
        );
    }

    private boolean isBaseProtectionWall(GridCoord coord) {
        TileType tile = tiles[coord.y()][coord.x()];
        if (tile != TileType.BRICK && tile != TileType.STEEL) {
            return false;
        }
        GridCoord base = level.basePosition();
        int dx = coord.x() - base.x();
        int dy = coord.y() - base.y();
        return (dy == -1 && Math.abs(dx) <= 1) || (dy == 0 && Math.abs(dx) == 1);
    }

    private void tickBaseShield(float delta) {
        baseShieldTimer = Math.max(0f, baseShieldTimer - delta);
    }

    private void tickExplosions(float delta) {
        explosions.forEach(explosion -> explosion.update(delta));
        explosions.removeIf(explosion -> !explosion.alive());
    }

    private void spawnEnemies(float delta) {
        if (enemies.size() >= maxEnemiesOnField()) {
            return;
        }
        for (WaveRuntime runtime : waves) {
            if (runtime.finished()) {
                continue;
            }
            runtime.timer -= delta;
            if (runtime.timer <= 0f && enemies.size() < maxEnemiesOnField()) {
                GridCoord spawn = runtime.nextSpawnPoint();
                float x = tileToWorldX(spawn.x());
                float y = tileToWorldY(spawn.y());
                Tank enemy = Tank.enemy(runtime.wave.type(), x, y);
                enemy.direction(Direction.DOWN);
                if (canOccupy(enemy, x, y)) {
                    enemies.add(enemy);
                    runtime.spawned++;
                    runtime.timer = currentSpawnInterval(runtime.wave.spawnEvery());
                } else {
                    runtime.timer = 0.35f;
                }
            }
            break;
        }
    }

    private float currentSpawnInterval(float baseInterval) {
        float progress = Math.min(1f, levelElapsedSeconds / GameConfig.ENEMY_SPAWN_RAMP_SECONDS);
        float multiplier = GameConfig.ENEMY_SPAWN_START_MULTIPLIER
                + (GameConfig.ENEMY_SPAWN_END_MULTIPLIER - GameConfig.ENEMY_SPAWN_START_MULTIPLIER) * progress;
        return Math.max(0.35f, baseInterval * multiplier);
    }

    private void checkLevelComplete() {
        if (status != GameStatus.PLAYING) {
            return;
        }
        boolean wavesDone = waves.stream().allMatch(WaveRuntime::finished);
        if (wavesDone && enemies.isEmpty()) {
            status = GameStatus.LEVEL_CLEAR;
            highScore = Math.max(highScore, score);
        }
    }

    public Rectangle baseBounds() {
        GridCoord base = level.basePosition();
        return new Rectangle(tileToWorldX(base.x()), tileToWorldY(base.y()), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
    }

    public List<GridCoord> overlappingTiles(Rectangle bounds) {
        List<GridCoord> coords = new ArrayList<>(4);
        int minX = Math.max(0, worldToGridX(bounds.x));
        int maxX = Math.min(level.width() - 1, worldToGridX(bounds.x + bounds.width - 0.1f));
        int minY = Math.max(0, worldToGridY(bounds.y + bounds.height - 0.1f));
        int maxY = Math.min(level.height() - 1, worldToGridY(bounds.y));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (inBounds(x, y)) {
                    coords.add(new GridCoord(x, y));
                }
            }
        }
        return coords;
    }

    public int worldToGridX(float worldX) {
        return (int) Math.floor(worldX / GameConfig.TILE_SIZE);
    }

    public int worldToGridY(float worldY) {
        int bottomRow = (int) Math.floor(worldY / GameConfig.TILE_SIZE);
        return level.height() - 1 - bottomRow;
    }

    public float tileToWorldX(int gridX) {
        return gridX * GameConfig.TILE_SIZE;
    }

    public float tileToWorldY(int gridY) {
        return (level.height() - 1 - gridY) * GameConfig.TILE_SIZE;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < level.width() && y >= 0 && y < level.height();
    }

    public int arenaWidth() {
        return level.width() * GameConfig.TILE_SIZE;
    }

    public int arenaHeight() {
        return level.height() * GameConfig.TILE_SIZE;
    }

    public TileType tileAt(int x, int y) {
        return tiles[y][x];
    }

    public void setTileForTest(int x, int y, TileType tile) {
        tiles[y][x] = tile;
    }

    public void setBaseShieldTimerForTest(float baseShieldTimer) {
        this.baseShieldTimer = Math.max(0f, baseShieldTimer);
    }

    public void addBulletForTest(Bullet bullet) {
        bullets.add(bullet);
    }

    public void addEnemyForTest(Tank enemy) {
        enemies.add(enemy);
    }

    public void addPowerUpForTest(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    public void applySnapshot(GameSnapshot snapshot) {
        levelIndex = snapshot.levelIndex;
        level = levels.get(levelIndex);
        tiles = new TileType[level.height()][level.width()];
        for (int y = 0; y < level.height(); y++) {
            String row = snapshot.tileRows.get(y);
            for (int x = 0; x < level.width(); x++) {
                tiles[y][x] = TileType.fromCode(row.charAt(x));
            }
        }
        status = snapshot.status;
        score = snapshot.score;
        highScore = snapshot.highScore;
        baseAlive = snapshot.baseAlive;
        baseShieldTimer = snapshot.baseShieldTimer;
        levelElapsedSeconds = snapshot.levelElapsedSeconds;

        players.clear();
        snapshot.players.stream().map(GameSnapshot.TankState::toTank).forEach(players::add);
        enemies.clear();
        snapshot.enemies.stream().map(GameSnapshot.TankState::toTank).forEach(enemies::add);
        bullets.clear();
        snapshot.bullets.stream().map(GameSnapshot.BulletState::toBullet).forEach(bullets::add);
        powerUps.clear();
        snapshot.powerUps.stream().map(GameSnapshot.PowerUpState::toPowerUp).forEach(powerUps::add);
        explosions.clear();
        snapshot.explosions.stream().map(GameSnapshot.ExplosionState::toExplosion).forEach(explosions::add);
    }

    public LevelDefinition level() {
        return level;
    }

    public int levelIndex() {
        return levelIndex;
    }

    public GameStatus status() {
        return status;
    }

    public Tank player() {
        return players.get(0);
    }

    public List<Tank> players() {
        return players;
    }

    public List<Tank> enemies() {
        return enemies;
    }

    public List<Bullet> bullets() {
        return bullets;
    }

    public List<PowerUp> powerUps() {
        return powerUps;
    }

    public List<Explosion> explosions() {
        return explosions;
    }

    public boolean baseAlive() {
        return baseAlive;
    }

    public boolean baseShielded() {
        return baseShieldTimer > 0f;
    }

    public float baseShieldTimer() {
        return baseShieldTimer;
    }

    public float levelElapsedSeconds() {
        return levelElapsedSeconds;
    }

    public int maxEnemiesOnField() {
        return Math.min(GameConfig.MAX_ENEMIES_ON_FIELD + 1, Math.max(3, 4 + levelIndex));
    }

    public int score() {
        return score;
    }

    public int highScore() {
        return highScore;
    }

    public void highScore(int highScore) {
        this.highScore = highScore;
    }

    private static final class WaveRuntime {
        private final EnemyWave wave;
        private int spawned;
        private int spawnCursor;
        private float timer;

        private WaveRuntime(EnemyWave wave) {
            this.wave = wave;
            this.timer = GameConfig.ENEMY_INITIAL_SPAWN_DELAY;
        }

        private GridCoord nextSpawnPoint() {
            GridCoord point = wave.spawnPoints().get(spawnCursor % wave.spawnPoints().size());
            spawnCursor++;
            return point;
        }

        private boolean finished() {
            return spawned >= wave.count();
        }
    }
}
