package me.srin.util.engine;

public class Coord extends Vec2 {
    private int increment_value;
    private boolean lock_x, lock_y;
    public Coord(int x, int y, int increment_value, boolean lock_x, boolean lock_y) {
        super(x, y);
        this.increment_value = increment_value;
        this.lock_x = lock_x;
        this.lock_y = lock_y;
    }

    public Coord() {
        this(0, 0, 1, false, false);
    }
    public Coord(int x, int y) {
        this(x, y, 1, false, false);
    }
    public Coord(int x, int y, int increment_value) {
        this(x, y, increment_value, false, false);
    }

    public int getIncrement_value() {
        return increment_value;
    }

    public void setIncrement_value(int increment_value) {
        this.increment_value = increment_value;
    }

    public void setLock_x(boolean lock_x) {
        this.lock_x = lock_x;
    }

    public void setLock_y(boolean lock_y) {
        this.lock_y = lock_y;
    }

    public boolean isLock_x() {
        return lock_x;
    }

    public boolean isLock_y() {
        return lock_y;
    }
}
