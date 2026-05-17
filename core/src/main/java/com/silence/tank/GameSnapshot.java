package com.silence.tank;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class GameSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int levelIndex;
    public GameStatus status;
    public int score;
    public int highScore;
    public boolean baseAlive;
    public List<String> tileRows;
    public List<TankState> players;
    public List<TankState> enemies;
    public List<BulletState> bullets;
    public List<PowerUpState> powerUps;
    public List<ExplosionState> explosions;

    public static GameSnapshot from(GameWorld world) {
        GameSnapshot snapshot = new GameSnapshot();
        snapshot.levelIndex = world.levelIndex();
        snapshot.status = world.status();
        snapshot.score = world.score();
        snapshot.highScore = world.highScore();
        snapshot.baseAlive = world.baseAlive();
        snapshot.tileRows = new ArrayList<>();
        for (int y = 0; y < world.level().height(); y++) {
            StringBuilder row = new StringBuilder(world.level().width());
            for (int x = 0; x < world.level().width(); x++) {
                row.append(world.tileAt(x, y).code);
            }
            snapshot.tileRows.add(row.toString());
        }
        snapshot.players = world.players().stream().map(TankState::from).toList();
        snapshot.enemies = world.enemies().stream().map(TankState::from).toList();
        snapshot.bullets = world.bullets().stream().map(BulletState::from).toList();
        snapshot.powerUps = world.powerUps().stream().map(PowerUpState::from).toList();
        snapshot.explosions = world.explosions().stream().map(ExplosionState::from).toList();
        return snapshot;
    }

    public static final class TankState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public boolean player;
        public EnemyType enemyType;
        public float x;
        public float y;
        public Direction direction;
        public int health;
        public int lives;
        public float fireCooldown;
        public float shieldTimer;
        public float speedTimer;
        public float powerShotTimer;
        public boolean alive;

        public static TankState from(Tank tank) {
            TankState state = new TankState();
            state.player = tank.isPlayer();
            state.enemyType = tank.enemyType();
            state.x = tank.x();
            state.y = tank.y();
            state.direction = tank.direction();
            state.health = tank.health();
            state.lives = tank.lives();
            state.fireCooldown = tank.fireCooldown();
            state.shieldTimer = tank.shieldTimer();
            state.speedTimer = tank.speedTimer();
            state.powerShotTimer = tank.powerShotTimer();
            state.alive = tank.alive();
            return state;
        }

        public Tank toTank() {
            if (player) {
                return Tank.restoredPlayer(x, y, direction, health, lives, fireCooldown, shieldTimer, speedTimer, powerShotTimer, alive);
            }
            return Tank.restoredEnemy(enemyType, x, y, direction, health, fireCooldown, alive);
        }
    }

    public static final class BulletState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public boolean fromPlayer;
        public boolean powerShot;
        public float x;
        public float y;
        public Direction direction;

        public static BulletState from(Bullet bullet) {
            BulletState state = new BulletState();
            state.fromPlayer = bullet.fromPlayer();
            state.powerShot = bullet.powerShot();
            state.x = bullet.x();
            state.y = bullet.y();
            state.direction = bullet.direction();
            return state;
        }

        public Bullet toBullet() {
            return new Bullet(fromPlayer, powerShot, x, y, direction);
        }
    }

    public static final class PowerUpState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public PowerUpType type;
        public float x;
        public float y;
        public float remainingSeconds;
        public boolean active;

        public static PowerUpState from(PowerUp powerUp) {
            PowerUpState state = new PowerUpState();
            state.type = powerUp.type();
            state.x = powerUp.x();
            state.y = powerUp.y();
            state.remainingSeconds = powerUp.remainingSeconds();
            state.active = powerUp.active();
            return state;
        }

        public PowerUp toPowerUp() {
            PowerUp powerUp = new PowerUp(type, x, y, remainingSeconds);
            if (!active) {
                powerUp.consume();
            }
            return powerUp;
        }
    }

    public static final class ExplosionState implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public float x;
        public float y;
        public float timer;

        public static ExplosionState from(Explosion explosion) {
            ExplosionState state = new ExplosionState();
            state.x = explosion.x();
            state.y = explosion.y();
            state.timer = explosion.timer();
            return state;
        }

        public Explosion toExplosion() {
            return new Explosion(x, y, timer);
        }
    }
}
