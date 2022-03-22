package me.srin.util;

public class GameOverWindow extends MainMenuWindow {
    public GameOverWindow(int x, int y) {
        super(
            x, y, "Game Over",
            CursString.create("Restart"),
            CursString.create("Quit")
        );
    }
}