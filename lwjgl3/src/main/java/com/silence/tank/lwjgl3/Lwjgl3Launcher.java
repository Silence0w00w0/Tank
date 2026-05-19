package com.silence.tank.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.silence.tank.GameConfig;
import com.silence.tank.LaunchOptions;
import com.silence.tank.TankGame;
import com.silence.tank.Texts;

public final class Lwjgl3Launcher {
    public static void main(String[] args) {
        LaunchOptions launchOptions = parseOptions(args);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(windowTitle(launchOptions));
        config.setWindowedMode(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        config.useVsync(true);
        new Lwjgl3Application(new TankGame(launchOptions), config);
    }

    private static String windowTitle(LaunchOptions launchOptions) {
        return switch (launchOptions.mode()) {
            case HOST -> Texts.HOST_TITLE;
            case CLIENT -> Texts.CLIENT_TITLE;
            case LOCAL -> Texts.GAME_TITLE;
        };
    }

    static LaunchOptions parseOptions(String[] args) {
        int port = LaunchOptions.DEFAULT_PORT;
        String connectHost = null;
        boolean host = false;
        boolean autoMode = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = true;
                case "--auto" -> autoMode = true;
                case "--connect" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--connect 需要主机地址。");
                    }
                    connectHost = args[++i];
                }
                case "--port" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--port 需要端口号。");
                    }
                    port = Integer.parseInt(args[++i]);
                }
                default -> throw new IllegalArgumentException("未知参数：" + args[i]);
            }
        }
        if (host && connectHost != null) {
            throw new IllegalArgumentException("--host 和 --connect 不能同时使用。");
        }
        if (host) {
            return LaunchOptions.host(port, autoMode);
        }
        if (connectHost != null) {
            return LaunchOptions.client(connectHost, port, autoMode);
        }
        return LaunchOptions.local(autoMode);
    }
}
