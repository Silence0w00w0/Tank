package com.silence.tank.net;

import com.silence.tank.InputCommand;

import java.io.Serial;

final class ClientInputMessage implements NetworkMessage {
    @Serial
    private static final long serialVersionUID = 1L;

    private final InputCommand command;

    ClientInputMessage(InputCommand command) {
        this.command = command.copy();
    }

    InputCommand command() {
        return command.copy();
    }
}
