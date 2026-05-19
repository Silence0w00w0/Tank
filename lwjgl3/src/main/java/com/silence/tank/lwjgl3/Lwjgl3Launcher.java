package com.silence.tank.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.silence.tank.GameConfig;
import com.silence.tank.LaunchOptions;
import com.silence.tank.TankGame;

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
            case HOST -> "Tank Host";
            case CLIENT -> "Tank Client";
            case LOCAL -> "Tank";
        };
    }

    private static LaunchOptions parseOptions(String[] args) {
        int port = LaunchOptions.DEFAULT_PORT;
        String connectHost = null;
        boolean host = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = true;
                case "--connect" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--connect requires a host address.");
                    }
                    connectHost = args[++i];
                }
                case "--port" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--port requires a number.");
                    }
                    port = Integer.parseInt(args[++i]);
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        if (host && connectHost != null) {
            throw new IllegalArgumentException("Use either --host or --connect, not both.");
        }
        if (host) {
            return LaunchOptions.host(port);
        }
        if (connectHost != null) {
            return LaunchOptions.client(connectHost, port);
        }
        return LaunchOptions.local();
    }
}
