package me.srin.util.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.*;
import static me.srin.Main.*;

public class LevelParser {
    private static final File DIRECTORY = new File(System.getProperty("user.dir") + "/levels/");
    public static final Pattern ARITHMETIC_PATTERN = Pattern.compile("(\\d+) *([+\\-*/]) *(\\d+)");
    static {
        if (!DIRECTORY.exists() && DIRECTORY.mkdirs()) {
            out.println("Created directory " + DIRECTORY.getAbsolutePath());
            exit(1);
        }
    }
    public static List<Supplier<Coord>> parse() throws IOException {
        List<Supplier<Coord>> coords = new ArrayList<>();
        File[] files = DIRECTORY.listFiles(it -> it.getName().endsWith(".snake"));
        if (files == null) {
            throw new RuntimeException("No levels found in " + DIRECTORY.getAbsolutePath());
        }
        final int width = gameWindow.getWidth();
        final int height = gameWindow.getHeight();
        for (File file : files) {
            if (file.isDirectory() || !file.exists() || !file.canRead()) continue;
            List<Runnable> actions = new ArrayList<>();
            Supplier<Coord> supplier = null;
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                String command_line = sc.nextLine().replace("$w", String.valueOf(width)).replace("$h", String.valueOf(height));
                String command = command_line;
                String[] args = null;
                if (command_line.matches("\\w+\\( *.+ *(, *.+ *)+\\)")) {
                    args = command_line.substring(command_line.indexOf('(') + 1, command_line.length() - 1).split(",");
                    command = command.substring(0, command_line.indexOf('('));
                }
                out.println(command);
                switch (command) {
                    case "hline" -> {
                        assert args != null;
                        final String[] argsFinal = args;
                        actions.add(() -> gameWindow.hline(
                                toInt(argsFinal[0].trim()),
                                toInt(argsFinal[1].trim()),
                                toInt(argsFinal[2].trim()),
                                toInt(argsFinal[3].trim())
                        ));
                    }
                    case "vline" -> {
                        assert args != null;
                        final String[] argsFinal = args;
                        actions.add(() -> gameWindow.vline(
                                toInt(argsFinal[0].trim()),
                                toInt(argsFinal[1].trim()),
                                toInt(argsFinal[2].trim()),
                                toInt(argsFinal[3].trim())
                        ));
                    }
                    case "box" -> actions.add(gameWindow::box);
                    case "return" -> {
                        assert args != null;
                        final String[] argsFinal = args;
                        supplier = () -> new Coord(
                                toInt(argsFinal[0].trim()),
                                toInt(argsFinal[1].trim()),
                                Integer.parseInt(argsFinal[2].trim()),
                                Boolean.parseBoolean(argsFinal[3].trim()),
                                Boolean.parseBoolean(argsFinal[4].trim())
                        );
                    }
                    case "border" -> {
                        assert args != null;
                        final String[] argsFinal = args;
                        actions.add(() -> gameWindow.border(
                                toBorder(argsFinal[0].trim()),
                                toBorder(argsFinal[1].trim()),
                                toBorder(argsFinal[2].trim()),
                                toBorder(argsFinal[3].trim()),
                                toBorder(argsFinal[4].trim()),
                                toBorder(argsFinal[5].trim()),
                                toBorder(argsFinal[6].trim()),
                                toBorder(argsFinal[7].trim())
                        ));
                    }
                }
            }
            final var supplierFinal = supplier;
            assert supplierFinal != null;
            coords.add(() -> {
                gameWindow.clear();
                actions.forEach(Runnable::run);
                gameWindow.refresh();
                return supplierFinal.get();
            });
        }
        return coords;
    }
    private static long toBorder(String expression) throws IllegalArgumentException {
        if (expression == null) throw new IllegalArgumentException("expression cannot be null");
        return switch (expression) {
            case "default_ul" -> ACS_ULCORNER;
            case "default_ur" -> ACS_URCORNER;
            case "default_ll" -> ACS_LLCORNER;
            case "default_lr" -> ACS_LRCORNER;
            default -> {
                if (!expression.matches("'.'")) throw new IllegalArgumentException("%s is an invalid argument".formatted(expression));
                yield expression.charAt(1);
            }
        };
    }
    private static int toInt(String expression) throws IllegalArgumentException {
        if (expression == null) throw new IllegalArgumentException("expression cannot be null");
        Matcher matcher = ARITHMETIC_PATTERN.matcher(expression);
        if (matcher.find()) {
            int num1 = Integer.parseInt(matcher.group(1));
            int num2 = Integer.parseInt(matcher.group(3));
            String operator = matcher.group(2);
            int val = switch (operator) {
                case "+" -> num1 + num2;
                case "-" -> num1 - num2;
                case "*" -> num1 * num2;
                case "/" -> num1 / num2;
                default -> throw new IllegalArgumentException("invalid operator %s".formatted(operator));
            };
            return toInt(matcher.replaceFirst(String.valueOf(val)).replace("(", "").replace(")", ""));
        } else return Integer.parseInt(expression);
    }
}
