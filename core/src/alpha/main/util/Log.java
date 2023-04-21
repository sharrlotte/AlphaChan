package alpha.main.util;

import alpha.main.ui.bot.Console;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class Log {

    private static String logHeader = new String();

    public static void printLine(String logHeader, Color color, Object content) {
        Platform.runLater(() -> {
            if (!logHeader.equals(Log.logHeader)) {
                Log.logHeader = logHeader;
                Console.appendLine(color, "");
            }

            Console.append(color, "[" + logHeader + "] " + content + "\n");
        });
    }

    public static void print(Color color, Object content) {
        Platform.runLater(() -> {
            Console.append(color, content.toString());
        });
    }

    public static void print(String logHeader, Object content) {
        printLine(logHeader, Color.WHITE, content);
    }

    public static void error(Exception e) {
        StringBuilder builder = new StringBuilder();

        builder.append(e.getMessage() + "\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.append("\t" + ste.toString() + "\n");
        }
        printLine("ERROR", Color.RED, builder.toString());

    }

    public static void error(Object content) {
        printLine("ERROR", Color.web("#DB2D20"), content);
    }

    public static void warning(Object content) {
        printLine("WARNING", Color.web("#FDED02"), content);
    }

    public static void system(Object content) {
        printLine("SYSTEM", Color.web("#01A0E4"), content);
    }

    public static void info(String logHeader, Object content) {
        printLine(logHeader, Color.GREEN, content);
    }
}
