package me.srin.util;
import me.srin.Main;

import static me.srin.Main.COLOR_PAIR;
import static me.srin.util.Color.RED_WHITE;
import static pdcurses.PdcursesLibrary.*;

public class MainMenuWindow extends Window {
    int maxItemLength = 0;
    int lastLinePosition = 2;
    int currentPointerPosition = 2;
    CursString[] menuItems;
    public MainMenuWindow(int x, int y, CursString... menuItems) {
        this(x, y, "Main Menu", menuItems);
    }
    MainMenuWindow(int x, int y, String title, CursString... menuItems) {
        super(
            x, y, 20, 4, true,
            CursString.create(title, RED_WHITE),
            Color.BLACK_WHITE
        );
        if (menuItems.length != 0) {
            addMenuItems(menuItems);
        } else {
            addMenuItems(
                CursString.create("Continue"),
                CursString.create("Restart"),
                CursString.create("Quit")
            );
        }
    }
    public void addMenuItems(CursString... menuItems) {
        this.menuItems = menuItems;
        resize_window(handle, height = height + menuItems.length, width);
        mvwin(handle, (Main.HEIGHT - height) / 2, (Main.WIDTH - width) / 2);
        clear();
        for (CursString menuItem : menuItems) {
            int length = menuItem.length();
            if (length > maxItemLength) {
                maxItemLength = length;
            }
            mvwaddstr(handle, lastLinePosition++, (width - (length % 2 == 0 ? length : length - 1)) / 2, menuItem.cstr());
        }
        renderPointer(2, true);
    }
    public void renderPointer(int pos, boolean flag) {
        int length = menuItems[pos - 2].length();
        int x = (width - (length % 2 == 0 ? length : length - 1)) / 2;
        wattron(handle, COLOR_PAIR(RED_WHITE));
        mvwaddch(handle, pos, x - 1, flag? '>': ' ');
        mvwaddch(handle, pos, x + length, flag? '<': ' ');
        wattroff(handle, COLOR_PAIR(RED_WHITE));
        refresh();
    }
    public int openWindow() {
        while (true) {
            renderPointer(currentPointerPosition, true);
            int ch = getch();
            switch (ch) {
                case 10 -> {
                    return menuItems[currentPointerPosition - 2].charAt(0);
                }
                case 'w' -> {
                    renderPointer(currentPointerPosition, false);
                    currentPointerPosition--;
                    if (currentPointerPosition < 2) {
                        currentPointerPosition = lastLinePosition - currentPointerPosition;
                    }
                }
                case 's' -> {
                    renderPointer(currentPointerPosition, false);
                    currentPointerPosition = (currentPointerPosition + 1) % (lastLinePosition);
                    if (currentPointerPosition == 0) currentPointerPosition = 2;
                }
            }
        }
    }
}
