package com.silence.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;
import java.util.Random;

public final class GameScreen extends ScreenAdapter {
    private static final String PREFS_NAME = "silence-tank";
    private static final String PREF_HIGH_SCORE = "highScore";
    private static final String PREF_LAST_LEVEL = "lastLevel";

    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final Viewport viewport = new FitViewport(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
    private final TextureAtlas atlas;
    private final Preferences preferences;
    private final GameWorld world;

    public GameScreen() {
        atlas = new TextureAtlas(Gdx.files.internal(AssetKeys.ATLAS));
        preferences = Gdx.app.getPreferences(PREFS_NAME);
        List<LevelDefinition> levels = List.of(
                LevelLoader.load(Gdx.files.internal("levels/level1.json")),
                LevelLoader.load(Gdx.files.internal("levels/level2.json")),
                LevelLoader.load(Gdx.files.internal("levels/level3.json"))
        );
        world = new GameWorld(levels, new Random());
        world.highScore(preferences.getInteger(PREF_HIGH_SCORE, 0));
        font.getData().setScale(1.18f);
        font.setUseIntegerPositions(false);
    }

    @Override
    public void render(float delta) {
        InputCommand command = readInput();
        world.update(delta, command);
        persistProgress();

        ScreenUtils.clear(0.025f, 0.028f, 0.032f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        drawArenaBackground();
        batch.begin();
        drawTerrain(false);
        drawBase();
        drawPowerUps();
        drawTanks();
        drawBullets();
        drawExplosions();
        drawTerrain(true);
        drawHud();
        drawStatusOverlay();
        batch.end();
    }

    private InputCommand readInput() {
        InputCommand command = InputCommand.none();
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            command.move(Direction.UP);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            command.move(Direction.RIGHT);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            command.move(Direction.DOWN);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            command.move(Direction.LEFT);
        }
        command.fire(Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.J));
        command.pause(Gdx.input.isKeyJustPressed(Input.Keys.P));
        command.restart(Gdx.input.isKeyJustPressed(Input.Keys.R));
        command.start(Gdx.input.isKeyJustPressed(Input.Keys.ENTER));
        return command;
    }

    private void drawArenaBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.09f, 0.10f, 1f);
        shapes.rect(0f, 0f, GameConfig.ARENA_WIDTH, GameConfig.ARENA_HEIGHT);
        shapes.setColor(0.13f, 0.15f, 0.16f, 1f);
        shapes.rect(0f, GameConfig.ARENA_HEIGHT, GameConfig.WINDOW_WIDTH, GameConfig.HUD_HEIGHT);
        shapes.end();
    }

    private void drawTerrain(boolean grassOnly) {
        LevelDefinition level = world.level();
        for (int y = 0; y < level.height(); y++) {
            for (int x = 0; x < level.width(); x++) {
                TileType tile = world.tileAt(x, y);
                if ((tile == TileType.GRASS) != grassOnly) {
                    continue;
                }
                if (tile.region != null) {
                    batch.draw(region(tile.region), world.tileToWorldX(x), world.tileToWorldY(y), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                }
            }
        }
    }

    private void drawBase() {
        if (!world.baseAlive()) {
            return;
        }
        Rectangle base = world.baseBounds();
        batch.draw(region(AssetKeys.BASE), base.x, base.y, base.width, base.height);
    }

    private void drawPowerUps() {
        for (PowerUp powerUp : world.powerUps()) {
            if (powerUp.active()) {
                batch.draw(region(powerUp.type().region), powerUp.x(), powerUp.y(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
            }
        }
    }

    private void drawTanks() {
        drawTank(world.player());
        for (Tank enemy : world.enemies()) {
            drawTank(enemy);
        }
    }

    private void drawTank(Tank tank) {
        TextureRegion texture = region(tank.region());
        batch.draw(
                texture,
                tank.x(),
                tank.y(),
                GameConfig.TILE_SIZE / 2f,
                GameConfig.TILE_SIZE / 2f,
                GameConfig.TILE_SIZE,
                GameConfig.TILE_SIZE,
                1f,
                1f,
                tank.direction().rotation
        );
        if (tank.hasShield()) {
            Color old = batch.getColor();
            batch.setColor(0.55f, 0.85f, 1f, 0.6f);
            batch.draw(region(AssetKeys.POWER_SHIELD), tank.x(), tank.y(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
            batch.setColor(old);
        }
    }

    private void drawBullets() {
        for (Bullet bullet : world.bullets()) {
            TextureRegion texture = region(bullet.fromPlayer() ? AssetKeys.BULLET_PLAYER : AssetKeys.BULLET_ENEMY);
            batch.draw(texture, bullet.x() - 4f, bullet.y() - 4f, 8f, 8f, 16f, 16f, 1f, 1f, bullet.direction().rotation);
        }
    }

    private void drawExplosions() {
        for (Explosion explosion : world.explosions()) {
            batch.draw(region(explosion.region()), explosion.x(), explosion.y(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
        }
    }

    private void drawHud() {
        font.setColor(Color.WHITE);
        float y = GameConfig.ARENA_HEIGHT + 58f;
        font.draw(batch, "LEVEL " + (world.levelIndex() + 1) + "  " + world.level().name(), 12f, y);
        font.draw(batch, "SCORE " + world.score(), 330f, y);
        font.draw(batch, "HIGH " + world.highScore(), 520f, y);
        font.draw(batch, "LIVES " + world.player().lives(), 700f, y);

        font.setColor(0.75f, 0.82f, 0.86f, 1f);
        font.draw(batch, "WASD/ARROWS MOVE   SPACE/J FIRE   P PAUSE   R RESTART", 12f, GameConfig.ARENA_HEIGHT + 24f);
    }

    private void drawStatusOverlay() {
        switch (world.status()) {
            case MENU -> drawCentered("TANK", "Press ENTER or SPACE to start");
            case PAUSED -> drawCentered("PAUSED", "Press P to continue");
            case LEVEL_CLEAR -> drawCentered("LEVEL CLEAR", "Press ENTER or wait for next level");
            case GAME_OVER -> drawCentered("GAME OVER", "Press R or ENTER to restart");
            case VICTORY -> drawCentered("VICTORY", "Press R or ENTER to play again");
            default -> {
            }
        }
    }

    private void drawCentered(String title, String subtitle) {
        font.setColor(Color.WHITE);
        font.getData().setScale(2.3f);
        layout.setText(font, title);
        font.draw(batch, title, (GameConfig.WINDOW_WIDTH - layout.width) / 2f, GameConfig.ARENA_HEIGHT / 2f + 70f);

        font.getData().setScale(1.2f);
        layout.setText(font, subtitle);
        font.draw(batch, subtitle, (GameConfig.WINDOW_WIDTH - layout.width) / 2f, GameConfig.ARENA_HEIGHT / 2f + 24f);
        font.getData().setScale(1.18f);
    }

    private TextureRegion region(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            throw new IllegalStateException("Missing atlas region: " + name);
        }
        return region;
    }

    private void persistProgress() {
        boolean dirty = false;
        if (world.highScore() > preferences.getInteger(PREF_HIGH_SCORE, 0)) {
            preferences.putInteger(PREF_HIGH_SCORE, world.highScore());
            dirty = true;
        }
        if (world.status() == GameStatus.LEVEL_CLEAR || world.status() == GameStatus.VICTORY) {
            int unlocked = Math.max(preferences.getInteger(PREF_LAST_LEVEL, 1), world.levelIndex() + 1);
            preferences.putInteger(PREF_LAST_LEVEL, unlocked);
            dirty = true;
        }
        if (dirty) {
            preferences.flush();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        atlas.dispose();
    }
}
