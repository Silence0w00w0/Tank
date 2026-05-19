package com.silence.tank;

public record LaunchOptions(GameMode mode, String host, int port, boolean autoMode) {
    public static final int DEFAULT_PORT = 54555;

    public static LaunchOptions local() {
        return local(false);
    }

    public static LaunchOptions local(boolean autoMode) {
        return new LaunchOptions(GameMode.LOCAL, "127.0.0.1", DEFAULT_PORT, autoMode);
    }

    public static LaunchOptions host(int port) {
        return host(port, false);
    }

    public static LaunchOptions host(int port, boolean autoMode) {
        return new LaunchOptions(GameMode.HOST, "0.0.0.0", port, autoMode);
    }

    public static LaunchOptions client(String host, int port) {
        return client(host, port, false);
    }

    public static LaunchOptions client(String host, int port, boolean autoMode) {
        return new LaunchOptions(GameMode.CLIENT, host, port, autoMode);
    }

    public boolean multiplayer() {
        return mode == GameMode.HOST || mode == GameMode.CLIENT;
    }
}
