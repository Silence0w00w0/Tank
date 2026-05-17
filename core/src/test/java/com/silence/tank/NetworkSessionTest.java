package com.silence.tank;

import com.silence.tank.net.ClientNetworkSession;
import com.silence.tank.net.HostNetworkSession;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkSessionTest {
    @Test
    void hostReceivesInputAndClientReceivesSnapshot() throws Exception {
        int port = freePort();
        try (
                HostNetworkSession host = new HostNetworkSession(port);
                ClientNetworkSession client = new ClientNetworkSession("127.0.0.1", port)
        ) {
            eventually(host::connected);

            client.sendInput(InputCommand.none().move(Direction.RIGHT).fire(true));
            eventually(() -> host.remoteInput().fire());
            assertEquals(Direction.RIGHT, host.remoteInput().moveDirection());

            GameWorld world = new GameWorld(List.of(level()), new Random(2), 2);
            world.startNewGame();
            host.sendSnapshot(GameSnapshot.from(world));
            eventually(() -> client.latestSnapshot() != null);
            assertEquals(2, client.latestSnapshot().players.size());
        }
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void eventually(BooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50L);
        }
        assertTrue(condition.getAsBoolean());
    }

    private static LevelDefinition level() {
        return LevelLoader.load("""
                {
                  "name": "Network",
                  "width": 5,
                  "height": 4,
                  "player": { "x": 1, "y": 3 },
                  "players": [
                    { "x": 1, "y": 3 },
                    { "x": 3, "y": 3 }
                  ],
                  "base": { "x": 2, "y": 2 },
                  "tiles": [
                    ".....",
                    ".BBB.",
                    ".B.B.",
                    ".BBB."
                  ]
                }
                """);
    }
}
