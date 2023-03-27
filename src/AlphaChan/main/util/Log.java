package AlphaChan.main.util;

public class Log {

    public enum TextColor {
        RESET("\033[0m"),

        BLACK("\033[0;30m"), //
        GREEN("\u001B[32m"), //
        RED("\u001B[31m"), //
        YELLOW("\u001B[33m"), //
        BLUE("\u001B[34m"), //
        MAGENTA("\033[0;35m"), //
        CYAN("\033[0;36m"), //
        WHITE("\033[0;37m"),

        BLACK_UNDERLINED("\033[4;30m"), //
        RED_UNDERLINED("\033[4;31m"), //
        GREEN_UNDERLINED("\033[4;32m"), //
        YELLOW_UNDERLINED("\033[4;33m"), //
        BLUE_UNDERLINED("\033[4;34m"), //
        MAGENTA_UNDERLINED("\033[4;35m"), //
        CYAN_UNDERLINED("\033[4;36m"), //
        WHITE_UNDERLINED("\033[4;37m"),

        BLACK_BACKGROUND("\033[40m"), //
        RED_BACKGROUND("\033[41m"), //
        GREEN_BACKGROUND("\033[42m"), //
        YELLOW_BACKGROUND("\033[43m"), //
        BLUE_BACKGROUND("\033[44m"), //
        MAGENTA_BACKGROUND("\033[45m"), //
        CYAN_BACKGROUND("\033[46m"), //
        WHITE_BACKGROUND("\033[47m");

        private final String color;

        TextColor(String color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return this.color;
        }
    }

    private static String logHeader = new String();

    public static void print(String logHeader, Object content, TextColor... colors) {
        if (!logHeader.equals(Log.logHeader)) {
            Log.logHeader = logHeader;
            System.out.println();
        }

        StringBuilder builder = new StringBuilder();
        for (TextColor color : colors) {
            builder.append(color);
        }

        System.out.println(builder.toString() + "<" + logHeader + "> " + content + TextColor.MAGENTA);
    }

    public static void print(String logHeader, Exception e, TextColor... colors) {
        StringBuilder builder = new StringBuilder();

        builder.append(e.getMessage() + "\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.append("\t" + ste.toString() + "\n");
        }

        print(logHeader, builder.toString(), colors);
    }

    public static void error(Object content) {
        print("ERROR", content, TextColor.RED);
    }

    public static void error(Exception e) {
        print("ERROR", e, TextColor.RED);
    }

    public static void warning(Object content) {
        print("WARNING", content, TextColor.YELLOW);
    }

    public static void system(Object content) {
        print("SYSTEM", content, TextColor.BLUE);
    }

    public static void info(String logHeader, Object content) {
        print(logHeader, content, TextColor.GREEN);
    }

    public static void print(String logHeader, Object content) {
        print(logHeader, content, TextColor.RESET);
    }
}
