package com.silence.tank.net;

import com.silence.tank.GameSnapshot;

import java.io.Serial;

final class HostSnapshotMessage implements NetworkMessage {
    @Serial
    private static final long serialVersionUID = 1L;

    private final GameSnapshot snapshot;

    HostSnapshotMessage(GameSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    GameSnapshot snapshot() {
        return snapshot;
    }
}
