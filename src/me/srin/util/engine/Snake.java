package me.srin.util.engine;

import me.srin.util.CursString;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.System.exit;
import static me.srin.Main.*;
import static me.srin.util.Color.BLUE_BLUE;
import static me.srin.util.Color.WHITE_RED;

public class Snake {
    private final Body body;
    private Coord start;
    private final int bodyTexture;
    private int level = 0;
    private int points = 0;
    private int size;
    private static List<Supplier<Coord>> levels;
    public static int levels_size;
    static {
        try {
            levels = LevelParser.parse();
            levels_size = levels.size();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }
    }
    private static final List<Supplier<Coord>> walls = List.of(
        () -> new Coord(gameWindow.getWidth() / 2, gameWindow.getHeight() / 2, -1, false, true),
        () -> {
            gameWindow.box();
            gameWindow.refresh();
            return new Coord(gameWindow.getWidth() / 2, gameWindow.getHeight() / 2, -1, false, true);
        }, () -> {
            int w = gameWindow.getWidth();
            int h = gameWindow.getHeight();
            gameWindow.hline((w - 20) / 2, (h - 5) / 2, 0, 20);
            gameWindow.hline((w - 20) / 2, (h - 5) / 2 + 5, 0, 20);
            gameWindow.border(' ', ' ', ' ', ' ', ACS_ULCORNER, ACS_URCORNER, ACS_LLCORNER, ACS_LRCORNER);
            gameWindow.hline(1, 0, 0, 7);
            gameWindow.vline(0, 1, 0, 4);

            gameWindow.hline(w - 1 - 7, 0, 0, 7);
            gameWindow.vline(w - 1, 1, 0, 4);

            gameWindow.hline(1, h - 1, 0, 7);
            gameWindow.vline(0, h - 1 - 4, 0, 4);

            gameWindow.hline(w - 1 - 7, h - 1, 0, 7);
            gameWindow.vline(w - 1, h - 1 - 4, 0, 4);
            gameWindow.refresh();
            return new Coord((w - 20) / 2 + 8, (h - 5) / 2 + 2, -1, true, false);
        }
    );
    public Snake(
        int bodyTexture
    ) {
        this.bodyTexture = bodyTexture;
        body = new Body(gameWindow.getWidth() * gameWindow.getHeight());
        reset(levels.get(0).get());
    }
    public void reset(Coord coord) {
        size = 3;
        body.reset();
        start = coord;
        int x = coord.getX();
        int y = coord.getY();
        var isXLocked = coord.isLock_x();
        var isYLocked = coord.isLock_y();
        gameWindow.attron(COLOR_PAIR(WHITE_RED));
        for (int i = size - 1; i >= 0; i--) {
            int _x = (isXLocked)? x + i: x;
            int _y = (isYLocked)? y + i: y;
            gameWindow.addch(_x, _y, bodyTexture);
            body.push(_x, _y);
        }
        gameWindow.attroff(COLOR_PAIR(WHITE_RED));
        int rnd_x = (int) (Math.random() % (gameWindow.getX() - 3) + 1);
        int rnd_y = (int) (Math.random() % (gameWindow.getY() - 3) + 1);
        gameWindow.addch(rnd_x, rnd_y, 'o');
        gameWindow.refresh();
        gameWindow.getch();
    }
    public void reset() {
        points = 0;
        level = 0;
        scoreWindow.hline(3, 1, ' ', 20);
        scoreWindow.print(3, 1, CursString.create("Points: 0"));
        scoreWindow.attron(COLOR_PAIR(BLUE_BLUE));
        scoreWindow.hline(3, 2, ' ', 20);
        scoreWindow.attroff(COLOR_PAIR(BLUE_BLUE));
        scoreWindow.hline(3, 4, ' ', 20);
        scoreWindow.print(3, 4, CursString.create("Level: 0"));
        scoreWindow.refresh();
        selectDifficulty();
        reset(levels.get(0).get());
    }
    public boolean move(int x, int y) {
        if (start.isLock_y()) {
            int gameWindowHeight = gameWindow.getHeight();
            var _y = (start.getY() + start.getIncrement_value()) % gameWindowHeight;
            if (_y < 0) _y += gameWindowHeight;
            start.setY(_y);
        }
        else if (start.isLock_x()) {
            int gameWindowWidth = gameWindow.getWidth();
            var _x = (start.getX() + start.getIncrement_value()) % gameWindowWidth;
            if (_x < 0) _x += gameWindowWidth;
            start.setX(_x);
        }
        var charachter_at_snek_mouth = gameWindow.inch(start.getX(), start.getY()) & A_CHARTEXT;
        gameWindow.attron(COLOR_PAIR(WHITE_RED));
        gameWindow.addch(start.getX(), start.getY(), bodyTexture);
        gameWindow.attroff(COLOR_PAIR(WHITE_RED));
        body.push(start.getX(), start.getY());
        if (charachter_at_snek_mouth != 'o' && charachter_at_snek_mouth != '*') {
            var end = body.pop();
            gameWindow.attron(COLOR_PAIR(BLUE_BLUE));
            gameWindow.addch(end.getX(), end.getY(), ' ');
            gameWindow.attroff(COLOR_PAIR(BLUE_BLUE));
            gameWindow.refresh();
        } else if (charachter_at_snek_mouth == '*') {
            onLevelUp();
        } else {
            var ch = gameWindow.inch(x, y) & A_CHARTEXT;
            while (ch == bodyTexture || ch == 113 || ch == 120) {
                x = random.nextInt(gameWindow.getWidth() - 3) + 1;
                y = random.nextInt(gameWindow.getHeight() - 3) + 1;
                ch = gameWindow.inch(x, y) & A_CHARTEXT;
            }
            var food = (size - 3) == 19? '*': 'o';
            gameWindow.addch(x, y, food);
            size++;
            points += 2;
            onSnakeGrow();
            gameWindow.refresh();
        }
        return charachter_at_snek_mouth == 113 || charachter_at_snek_mouth == 120 || charachter_at_snek_mouth == bodyTexture;
    }
    public void onSnakeGrow() {
        scoreWindow.print(3, 1, CursString.create("Points: %d".formatted(points)));
        scoreWindow.attron(A_REVERSE);
        scoreWindow.hline(3, 2, ' ', size - 3);
        scoreWindow.attroff(A_REVERSE);
        scoreWindow.refresh();
    }
    public void onLevelUp() {
        scoreWindow.attron(COLOR_PAIR(BLUE_BLUE));
        scoreWindow.hline(3, 2, ' ', 20);
        scoreWindow.attroff(COLOR_PAIR(BLUE_BLUE));
        scoreWindow.print(3, 4, CursString.create("Level: %d".formatted(level)));
        scoreWindow.refresh();
        reset(levels.get(++level % levels_size).get());
    }
    public void up() {
        if (!start.isLock_y()) {
            start.setIncrement_value(-1);
            start.setLock_y(true);
            start.setLock_x(false);
        }
    }
    public void down() {
        if (!start.isLock_y()) {
            start.setIncrement_value(1);
            start.setLock_y(true);
            start.setLock_x(false);
        }
    }
    public void right() {
        if (!start.isLock_x()) {
            start.setIncrement_value(1);
            start.setLock_y(false);
            start.setLock_x(true);
        }
    }
    public void left() {
        if (!start.isLock_x()) {
            start.setIncrement_value(-1);
            start.setLock_y(false);
            start.setLock_x(true);
        }
    }
    static class Body {
        private final int size;
        Vec2[] data;
        int head;
        int tail;
        Body(int size) {
            this.size = size;
            head = 0;
            tail = 0;
            data = new Vec2[size];
        }
        public void push(int x, int y) {
            data[head] = new Vec2(x, y);
            head = (head + 1) % size;
        }
        public Vec2 pop() {
            var data = this.data[tail];
            tail = (tail + 1) % size;
            return data;
        }

        public void reset() {
            head = 0;
            tail = 0;
        }

        @Override
        public String toString() {
            return "Body {" +
                "size = " + size +
                ", head = " + head +
                ", tail = " + tail +
            '}';
        }
    }
}
