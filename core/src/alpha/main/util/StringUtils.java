package alpha.main.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
                    point += i * 2;
            }

            if (point > max) {
                max = point;
                estimate = l;
            }

        }
        return max > -diff ? estimate : null;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static int getMatchPoint(String s1, String s2) {
        // String longer = s1, shorter = s2;
        // if (s1.length() < s2.length()) {
        // longer = s2;
        // shorter = s1;
        // }
        // int longerLength = longer.length();
        // if (longerLength == 0) {
        // return 1.0;
        // }

        // return (longerLength - editDistance(longer, shorter)) / (double)
        // longerLength;

        int point = 0;

        if (s2.startsWith(s1))
            point += 1;

        for (int i = 1; i < s1.length() + 1; i++) {
            if (s2.contains(s1.substring(0, i))) {
                point += i;
            } else
                break;
        }

        return point;
    }

    public static List<String> findBestMatches(String input, List<String> list, int maxOption) {

        if (list.isEmpty() || input == null)
            return list;

        List<String> result = new LinkedList<>();

        list = new ArrayList<>(list);
        list.sort((o1, o2) -> (getMatchPoint(input, o2) - getMatchPoint(input, o1)));

        for (int i = 0; i < (maxOption < list.size() ? maxOption : list.size()); i++) {
            String option = list.get(i);
            result.add(option);
        }
        return result;
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
