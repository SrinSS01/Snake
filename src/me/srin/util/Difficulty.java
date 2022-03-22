package me.srin.util;

public enum Difficulty {
    INSANE(27),
    HARD(40),
    EASY(100),
    NOOB(200);

    private final int value;
    Difficulty(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
