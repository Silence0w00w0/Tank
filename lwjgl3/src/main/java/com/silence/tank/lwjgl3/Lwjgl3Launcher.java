package com.silence.tank.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.silence.tank.GameConfig;
import com.silence.tank.TankGame;

public final class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Tank");
        config.setWindowedMode(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.useVsync(true);
        new Lwjgl3Application(new TankGame(), config);
    }
}
