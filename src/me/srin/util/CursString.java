package me.srin.util;

import org.bridj.Pointer;

public record CursString(String string, Color color) {
    public static CursString create(String string, Color color) {
        return new CursString(string, color);
    }
    public static CursString create(String string) {
        return new CursString(string, Color.BLACK_WHITE);
    }
    public int length() {
        return string.length();
    }
    public char charAt(int index) {
        return string.charAt(index);
    }
    public Pointer<Byte> cstr() {
        return Pointer.pointerToCString(string);
    }
    @Override
    public String toString() {
        return string;
    }
}
