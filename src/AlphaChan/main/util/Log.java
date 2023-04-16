package AlphaChan.main.util;

import AlphaChan.main.ui.Console;

import javafx.application.Platform;
import javafx.scene.paint.Color;

public class Log {

    private static String logHeader = new String();

    public static void print(String logHeader, Object content, Color color) {
        Platform.runLater(() -> {
            if (!logHeader.equals(Log.logHeader)) {
                Log.logHeader = logHeader;
                Console.appendLine(color, "");
            }

            Console.appendLine(color, "[" + logHeader + "] " + content);
        });
    }

    public static void print(Object content, Color color) {
        Platform.runLater(() -> {
            Console.appendLine(color, content.toString());
        });
    }

    public static void print(String logHeader, Object content) {
        print(logHeader, content, Color.WHITE);
    }

    public static void error(Exception e) {
        StringBuilder builder = new StringBuilder();

        builder.append(e.getMessage() + "\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.append("\t" + ste.toString() + "\n");
        }
        print("ERROR", builder.toString(), Color.RED);

    }

    public static void error(Object content) {
        print("ERROR", content, Color.web("#DB2D20"));
    }

    public static void warning(Object content) {
        print("WARNING", content, Color.web("#FDED02"));
    }

    public static void system(Object content) {
        print("SYSTEM", content, Color.web("#01A0E4"));
    }

    public static void info(String logHeader, Object content) {
        print(logHeader, content, Color.GREEN);
    }
}
