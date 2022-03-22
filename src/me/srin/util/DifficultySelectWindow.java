package me.srin.util;

public class DifficultySelectWindow extends MainMenuWindow {
    public DifficultySelectWindow(int x, int y) {
        super(
            x, y, "difficulty",
            CursString.create("Noob"),
            CursString.create("Easy"),
            CursString.create("Hard"),
            CursString.create("Insane"),
            CursString.create("Quit")
        );
    }
}