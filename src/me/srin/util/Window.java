package me.srin.util;

import org.bridj.Pointer;
import pdcurses.PdcursesLibrary;
import pdcurses.WINDOW;

import static me.srin.Main.*;
import static pdcurses.PdcursesLibrary.*;

public class Window {
    protected final int x;
    protected final int y;
    protected final int width;
    protected int height;
    protected final Pointer<WINDOW> handle;
    protected final boolean border;
    protected final CursString title;

    public Window(
        int x, int y,
        int width, int height,
        boolean border,
        CursString title,
        Color background
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        handle = newwin(height, width, y, x);
        wbkgd(handle, COLOR_PAIR(background));
        this.border = border;
        this.title = title;
        clear();
        refresh();
    }

    public Window(
            int x, int y,
            int width, int height,
            boolean border,
            Color background
    ) { this(x, y, width, height, border, null, background); }

    public void clear() {
        wclear(handle);
        if (border) {
            box();
        }
        if (title != null) {
            wattron(handle, COLOR_PAIR(title.color()));
            mvwaddstr(handle, 0, (width - title.length()) / 2, title.cstr());
            wattroff(handle, COLOR_PAIR(title.color()));
        }
    }

    public void box() {
        PdcursesLibrary.box(handle, 0, 0);
    }

    public void destroy() {
        delwin(handle);
    }

    public void refresh() {
        wrefresh(handle);
    }

    public void touch() {
        touchwin(handle);
    }

    public int getHeight() {
        return height;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getch() {
        return wgetch(handle);
    }
    public void attron(long type) {
        wattron(handle, type);
    }
    public void attroff(long type) {
        wattroff(handle, type);
    }
    public void hline(int x, int y, int ch, int length) {
        mvwhline(handle, y, x, ch, length);
    }
    public void vline(int x, int y, int ch, int length) {
        mvwvline(handle, y, x, ch, length);
    }
    public void border(long ch1, long ch2, long ch3, long ch4, long ch5, long ch6, long ch7, long ch8) {
        wborder(handle, ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8);
    }
    public void print(int x, int y, CursString text) {
        attron(COLOR_PAIR(text.color()));
        mvwaddstr(handle, y, x, text.cstr());
        attroff(COLOR_PAIR(text.color()));
    }
    public void addch(int x, int y, int ch) {
        mvwaddch(handle, y, x, ch);
    }

    public long inch(int x, int y) {
        return mvwinch(handle, y, x);
    }

    public void timeout(int delay) {
        wtimeout(handle, delay);
    }

    public void timeout(Difficulty difficulty) {
        timeout(difficulty.getValue());
    }
}
