package com.silence.tank;

public record LaunchOptions(GameMode mode, String host, int port) {
    public static final int DEFAULT_PORT = 54555;

    public static LaunchOptions local() {
        return new LaunchOptions(GameMode.LOCAL, "127.0.0.1", DEFAULT_PORT);
    }

    public static LaunchOptions host(int port) {
        return new LaunchOptions(GameMode.HOST, "0.0.0.0", port);
    }

    public static LaunchOptions client(String host, int port) {
        return new LaunchOptions(GameMode.CLIENT, host, port);
    }

    public boolean multiplayer() {
        return mode == GameMode.HOST || mode == GameMode.CLIENT;
    }
}
