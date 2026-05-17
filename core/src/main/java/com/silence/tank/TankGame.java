package com.silence.tank;

import com.badlogic.gdx.Game;

public final class TankGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
