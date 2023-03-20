package AlphaChan.main.util;

import java.util.Set;

public class StringUtils {

    public static String toTime(long duration) {
        if (duration == Long.MAX_VALUE)
            return "LIVE";
        long seconds = Math.round(duration / 1000.0);
        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        long minutes = seconds / 60;
        seconds %= 60;
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 ? "0" + minutes : minutes) + ":"
                + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String toProgressBar(double percent, int barLength, String mark, String head, String tail) {
        String str = head;
        for (int i = 0; i < barLength; i++)
            if (i == (int) (percent * barLength) - 1)
                str += mark;
            else
                str += "▬";

        return str + tail;
    }

    public static String filter(String input) {
        return input.replace("\u202E", "")
                .replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }

    public static <T> String findBestMatch(String input, Set<T> list) {
        byte[] points = new byte[127];
        byte[] base = new byte[127];
        String estimate = null;
        int diff = 10;
        int max = -9999, point = 0;

        for (int i = 0; i < input.length(); i++) {
            base[input.charAt(i)] += 1;
        }

        for (Object t : list) {
            String l = t.toString();
            point = 0;

            for (int i = 0; i < 127; i++) {
                points[i] = 0;
            }

            for (int i = 0; i < l.length(); i++) {
                points[l.charAt(i)] += 1;
            }

            for (int i = 0; i < 127; i++) {
                point -= Math.abs(points[i] - base[i]);
            }

            if (point > max) {
                max = point;
                estimate = l;
            }

        }
        return max > -diff ? estimate : null;

    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}