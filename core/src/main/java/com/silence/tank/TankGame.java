package com.silence.tank;

import com.badlogic.gdx.Game;

public final class TankGame extends Game {
    private final LaunchOptions launchOptions;

    public TankGame() {
        this(LaunchOptions.local());
    }

    public TankGame(LaunchOptions launchOptions) {
        this.launchOptions = launchOptions;
    }

    @Override
    public void create() {
        setScreen(new GameScreen(launchOptions));
    }
}
