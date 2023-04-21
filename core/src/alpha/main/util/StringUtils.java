package alpha.main.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;

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

    public static String getProgressBar(double percent, int barLength, String mark, String head, String tail) {
        String str = head;
        for (int i = 0; i < barLength; i++)
            if (i == (int) (percent * barLength) - 1)
                str += mark;
            else
                str += "▬";

        return str + tail;
    }

    public static String filter(String input) {
        return input.replace("\u202E", "").replace("@everyone", "@\u0435veryone") // cyrillic letter e
                .replace("@here", "@h\u0435re") // cyrillic letter e
                .trim();
    }

    public static <T> String findBestMatch(String input, Set<T> list) {
        byte[] points = new byte[127];
        byte[] base = new byte[127];
        String estimate = null;
        int diff = 10;
        int max = 1, point = 0;

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
                point += Math.min(0, Math.abs(points[i] - base[i]));
            }

            for (int i = 2; i < input.length(); i++) {
                if (l.contains(input.substring(0, i)))
                    point += i;
            }

            if (point > max) {
                max = point;
                estimate = l;
            }

        }
        return max > -diff ? estimate : null;
    }

    public static <T> String listToLines(Iterable<T> list) {
        StringBuilder builder = new StringBuilder();

        Iterator<T> itr = list.iterator();
        T value;
        while (itr.hasNext()) {
            value = itr.next();
            builder.append(value.toString() + "\n");
        }

        return builder.toString();
    }

    public static <K, V> String mapToLines(Map<K, V> map) {
        StringBuilder builder = new StringBuilder();

        for (K key : map.keySet())
            builder.append(key + " = " + map.get(key).toString() + "\n");

        return builder.toString();
    }

    public static String capitalize(String str) {
        str = str.toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public static String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue())
                + format(value.getOpacity()))
                .toUpperCase();
    }

    public static String backtick(String str) {
        return "```" + str + "```";
    }
}
