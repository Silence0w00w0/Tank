package com.silence.tank;

public final class InputCommand {
    private Direction moveDirection;
    private boolean fire;
    private boolean pause;
    private boolean restart;
    private boolean start;

    public static InputCommand none() {
        return new InputCommand();
    }

    public Direction moveDirection() {
        return moveDirection;
    }

    public InputCommand move(Direction moveDirection) {
        this.moveDirection = moveDirection;
        return this;
    }

    public boolean fire() {
        return fire;
    }

    public InputCommand fire(boolean fire) {
        this.fire = fire;
        return this;
    }

    public boolean pause() {
        return pause;
    }

    public InputCommand pause(boolean pause) {
        this.pause = pause;
        return this;
    }

    public boolean restart() {
        return restart;
    }

    public InputCommand restart(boolean restart) {
        this.restart = restart;
        return this;
    }

    public boolean start() {
        return start;
    }

    public InputCommand start(boolean start) {
        this.start = start;
        return this;
    }
}
