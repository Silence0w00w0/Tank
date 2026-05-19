package com.silence.tank.lwjgl3;

import com.silence.tank.GameMode;
import com.silence.tank.LaunchOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Lwjgl3LauncherTest {
    @Test
    void parsesAutoLocalMode() {
        LaunchOptions options = Lwjgl3Launcher.parseOptions(new String[]{"--auto"});

        assertEquals(GameMode.LOCAL, options.mode());
        assertTrue(options.autoMode());
    }

    @Test
    void parsesHostAutoWithCustomPort() {
        LaunchOptions options = Lwjgl3Launcher.parseOptions(new String[]{"--host", "--auto", "--port", "54556"});

        assertEquals(GameMode.HOST, options.mode());
        assertEquals(54556, options.port());
        assertTrue(options.autoMode());
    }

    @Test
    void parsesClientAutoWithCustomPort() {
        LaunchOptions options = Lwjgl3Launcher.parseOptions(new String[]{"--connect", "192.168.1.23", "--auto", "--port", "54556"});

        assertEquals(GameMode.CLIENT, options.mode());
        assertEquals("192.168.1.23", options.host());
        assertEquals(54556, options.port());
        assertTrue(options.autoMode());
    }
}
