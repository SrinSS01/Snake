package me.srin;

import me.srin.util.*;
import me.srin.util.engine.Snake;
import org.bridj.Pointer;
import pdcurses.WINDOW;

import java.util.Random;
import java.util.logging.Logger;

import static java.lang.System.*;
import static java.util.logging.Level.*;
import static me.srin.util.Difficulty.*;
import static pdcurses.PdcursesLibrary.*;
import static me.srin.util.Color.*;

public final class Main implements AutoCloseable {
    static final Pointer<WINDOW> stdscr;
    public static final int WIDTH = 50;
    public static final int HEIGHT = 25;
    private static final int A_COLOR = 0xff000000;
    private static final int A_ALTCHARSET = 0x00010000;
    public static final int A_REVERSE = 0x00200000;
    public static final int A_CHARTEXT = 0x0000ffff;
    public static final long ACS_VLINE = PDC_ACS('x');
    public static final long ACS_HLINE = PDC_ACS('q');
    public static final long ACS_ULCORNER = PDC_ACS('l');
    public static final long ACS_URCORNER = PDC_ACS('k');
    public static final long ACS_LLCORNER = PDC_ACS('m');
    public static final long ACS_LRCORNER = PDC_ACS('j');
    private static volatile boolean shouldExit = false;
    private static volatile boolean isGameOver = false;
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final DifficultySelectWindow difficultySelectWindow;
    public static final Window gameWindow;
    public static final Window scoreWindow;
    private static final Window keyCallBackWindow;
    private static final GameOverWindow gameOverWindow;
    private static final MainMenuWindow mainMenuWindow;
    private static Snake snake;
    public static final Random RANDOM = new Random();
    static {
        stdscr = initscr();
        cbreak();
        noecho();
        curs_set(0);
        resize_term(HEIGHT, WIDTH);
        if (has_colors() == 0) {
            endwin();
            LOGGER.log(SEVERE, "Your terminal does not support color");
            exit(1);
        }
        start_color();
        init_pair(ordinal(RED_WHITE), (short) COLOR_RED, (short) COLOR_WHITE);
        init_pair(ordinal(BLACK_WHITE), (short) COLOR_BLACK, (short) COLOR_WHITE);
        init_pair(ordinal(WHITE_BLUE), (short) COLOR_WHITE, (short) COLOR_BLUE);
        init_pair(ordinal(BLUE_BLUE), (short) COLOR_BLUE, (short) COLOR_BLUE);
        init_pair(ordinal(WHITE_BLACK), (short) COLOR_WHITE, (short) COLOR_BLACK);
        init_pair(ordinal(WHITE_RED), (short) COLOR_WHITE, (short) COLOR_RED);
        difficultySelectWindow = new DifficultySelectWindow(
            (WIDTH - 20) / 2,
            (HEIGHT - 4) / 2
        );
        mainMenuWindow = new MainMenuWindow(
            (WIDTH - 20) / 2,
            (HEIGHT - 4) / 2
        );
        gameOverWindow = new GameOverWindow(
            (WIDTH - 20) / 2,
            (HEIGHT - 4) / 2
        );
        gameWindow = new Window(0, 0, WIDTH, HEIGHT - 7, false, WHITE_BLUE);
        scoreWindow = new Window(0, HEIGHT - 7, WIDTH, 7, true, RED_WHITE);
        keyCallBackWindow = new Window(
                WIDTH - 8,
                HEIGHT - 6,
                7, 5,
                false, RED_WHITE
        );
    }
    public static void main(String[] args) {
        try(Main ignored = new Main()) {
            scoreWindow.print(3, 1, CursString.create("Points: 0"));
            scoreWindow.print(3, 4, CursString.create("Level: 0"));
            scoreWindow.attron(COLOR_PAIR(BLUE_BLUE));
            scoreWindow.hline(3, 2, ' ', 20);
            scoreWindow.attroff(COLOR_PAIR(BLUE_BLUE));
            scoreWindow.refresh();
            keyCallBackWindow.addch(3, 1, 'w');
            keyCallBackWindow.addch(2, 2, 'a');
            keyCallBackWindow.addch(3, 3, 's');
            keyCallBackWindow.addch(4, 2, 'd');
            keyCallBackWindow.attron(COLOR_PAIR(BLACK_WHITE));
            keyCallBackWindow.addch(3, 0, '^');
            keyCallBackWindow.addch(0, 2, '<');
            keyCallBackWindow.addch(3, 4, 'v');
            keyCallBackWindow.addch(6, 2, '>');
            keyCallBackWindow.attroff(COLOR_PAIR(BLACK_WHITE));
            keyCallBackWindow.refresh();
            selectDifficulty();
            snake = new Snake('+');
            while (!shouldExit) {
                int key = keyCallBackWindow.getch();
                switch (key) {
                    case 27, 'q' -> {
                        if (!isGameOver) {
                            showMainMenu();
                        }
                    }
                    case 'w' -> snake.up();
                    case 'a' -> snake.left();
                    case 's' -> snake.down();
                    case 'd' -> snake.right();
                }
                int rnd_x = RANDOM.nextInt(gameWindow.getWidth() - 3) + 1;
                int rnd_y = RANDOM.nextInt(gameWindow.getHeight() - 3) + 1;
                isGameOver = snake.move(rnd_x, rnd_y);
                if (isGameOver) gameOver();
            }
        } catch (Exception e) {
            LOGGER.log(SEVERE, "[ERROR]: ", e);
        }
    }
    static void cleanup(Window... windows) {
        for (var window : windows) {
            window.destroy();
        }
        endwin();
    }
    public static void selectDifficulty() {
        difficultySelectWindow.touch();
        var ch = difficultySelectWindow.openWindow();
        gameWindow.touch();
        gameWindow.refresh();
        switch (ch) {
            case 'Q' -> shouldExit = true;
            case 'N' -> keyCallBackWindow.timeout(NOOB);
            case 'E' -> keyCallBackWindow.timeout(EASY);
            case 'H' -> keyCallBackWindow.timeout(HARD);
            case 'I' -> keyCallBackWindow.timeout(INSANE);
        }
    }
    static void gameOver() {
        gameOverWindow.touch();
        var ch = gameOverWindow.openWindow();
        gameWindow.touch();
        gameWindow.refresh();
        switch (ch) {
            case 'Q' -> shouldExit = true;
            case 'R' -> snake.reset();
        }
        isGameOver = false;
    }
    static void showMainMenu() {
        mainMenuWindow.touch();
        var ch = mainMenuWindow.openWindow();
        gameWindow.touch();
        gameWindow.refresh();
        switch (ch) {
            case 'Q' -> shouldExit = true;
            case 'R' -> snake.reset();
        }
    }
    public static long COLOR_PAIR(Color color) {
        long n = ordinal(color);
        return (n << PDC_COLOR_SHIFT) & A_COLOR;
    }
    public static long PDC_ACS(int ch) {
        return ch | A_ALTCHARSET;
    }
    static short ordinal(Color color) {
        return (short) (color.ordinal() + 1);
    }

    @Override
    public void close() {
        cleanup(
                scoreWindow,
                gameWindow,
                difficultySelectWindow,
                mainMenuWindow,
                gameOverWindow,
                keyCallBackWindow
        );
    }
}
