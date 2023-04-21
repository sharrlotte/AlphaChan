package alpha.main.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alpha.main.util.Log;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class LocaleManager {

    public static final DiscordLocale DEFAULT_LOCALE = DiscordLocale.ENGLISH_US;

    private static final DiscordLocale[] SUPPORTED_LOCALE = { DEFAULT_LOCALE, DiscordLocale.VIETNAMESE };

    private static final String LOCALE_FOLDER_PATH = "locale/";
    private static final Pattern KEY_PATTERN = Pattern
            .compile("<([a-z,A-Z0-9\\._]+)>\\[([a-z,A-Z0-9:\\._'%/\\n\\s\\t\\r]+)\\]");

    private static ConcurrentHashMap<DiscordLocale, Bundle> bundles;
    private static LocaleManager localeManager;

    private LocaleManager() {
        bundles = new ConcurrentHashMap<>();
        bundles.put(DEFAULT_LOCALE, new Bundle(DEFAULT_LOCALE, LOCALE_FOLDER_PATH));
    }

    public synchronized static LocaleManager getInstance() {
        if (localeManager == null)
            localeManager = new LocaleManager();

        return localeManager;
    }

    public static boolean isLocaleSupported(DiscordLocale locale) {
        for (DiscordLocale sl : SUPPORTED_LOCALE) {
            if (sl.equals(locale))
                return true;
        }
        return false;
    }

    public static String format(Guild guild, String s) {
        return format(guild.getLocale(), s);
    }

    public static String format(Guild guild, String s, Object... obj) {
        return String.format(format(guild.getLocale(), s), obj);
    }

    public static String format(DiscordLocale discordLocale, String s) {
        if (isLocaleSupported(discordLocale)) {
            Matcher matcher = KEY_PATTERN.matcher(s);
            while (matcher.find()) {
                String key = matcher.group(1);
                String def = matcher.group(2);
                String value = getOrDefault(discordLocale, key, def);

                s = s.replace(matcher.group(), value);
            }
        }

        return s;
    }

    public static String getOrDefault(DiscordLocale DiscordLocale, String key, String def) {
        Bundle bundle = bundles.get(DiscordLocale);
        if (bundle == null) {
            if (DiscordLocale == DEFAULT_LOCALE)
                throw new IllegalStateException("Default locale not found");

            bundle = new Bundle(DiscordLocale, LOCALE_FOLDER_PATH);
            bundles.put(DiscordLocale, bundle);
        }

        return bundle.getOrDefault(key, def);
    }

    public static class Bundle {

        private OrderedProperties properties;
        private DiscordLocale DiscordLocale;
        private String localeFolderPath;

        private Bundle(DiscordLocale DiscordLocale, String localeFolderPath) {

            this.DiscordLocale = DiscordLocale;
            this.localeFolderPath = localeFolderPath;

            try {

                File folder = new File(localeFolderPath);
                if (!folder.exists())
                    folder.mkdirs();

                File file = new File(getLocaleFilePath());
                if (!file.exists())
                    file.createNewFile();

                InputStream input = new FileInputStream(file);
                properties = new OrderedProperties();
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));

                input.close();

            } catch (IOException e) {
                Log.error(e);
            }
        }

        public String getLocaleFilePath() {
            return localeFolderPath + DiscordLocale.name() + ".properties";
        }

        public void save() {
            try (OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(getLocaleFilePath()),
                    StandardCharsets.UTF_8)) {

                properties.store(output);

            } catch (IOException io) {
                Log.error(io);
            }
        }

        private String getOrDefault(String key, String def) {
            String value = properties.getProperty(key);
            if (value == null) {
                if (DiscordLocale == DEFAULT_LOCALE) {
                    properties.put(key, def);
                    save();
                    return def;
                }
                value = LocaleManager.getOrDefault(DEFAULT_LOCALE, key, def);
                properties.put(key, value);
                save();
                return value;

            }

            return value;
        }
    }

    private static class OrderedProperties extends Properties {
        @Override
        public synchronized Enumeration<Object> keys() {
            List<Object> list = new ArrayList<>(super.keySet());
            Comparator<Object> comparator = Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(list, comparator);
            return Collections.enumeration(list);
        }

        public void store(Writer writer)
                throws IOException {
            store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer
                    : new BufferedWriter(writer),
                    false);
        }

        private void store0(BufferedWriter bw, boolean escUnicode)
                throws IOException {

            bw.write("#" + new Date().toString());
            bw.newLine();
            synchronized (this) {

                LinkedList<SimpleEntry<String, String>> list = new LinkedList<>();

                for (Map.Entry<Object, Object> e : entrySet()) {
                    String key = (String) e.getKey();
                    String val = (String) e.getValue();
                    list.add(new SimpleEntry<String, String>(key, val));
                }

                Comparator<Object> comparator = Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER);
                Collections.sort(list, comparator);

                for (SimpleEntry<String, String> e : list) {
                    String key = e.getKey();
                    String val = e.getValue();

                    key = saveConvert(key, true, escUnicode);
                    /*
                     * No need to escape embedded and trailing spaces for value, hence
                     * pass false to flag.
                     */
                    val = saveConvert(val, false, escUnicode);
                    bw.write(key + "=" + val);
                    bw.newLine();
                }
            }
            bw.flush();
        }

        private String saveConvert(String theString,
                boolean escapeSpace,
                boolean escapeUnicode) {
            int len = theString.length();
            int bufLen = len * 2;
            if (bufLen < 0) {
                bufLen = Integer.MAX_VALUE;
            }
            StringBuilder outBuffer = new StringBuilder(bufLen);
            HexFormat hex = HexFormat.of().withUpperCase();
            for (int x = 0; x < len; x++) {
                char aChar = theString.charAt(x);
                // Handle common case first, selecting largest block that
                // avoids the specials below
                if ((aChar > 61) && (aChar < 127)) {
                    if (aChar == '\\') {
                        outBuffer.append('\\');
                        outBuffer.append('\\');
                        continue;
                    }
                    outBuffer.append(aChar);
                    continue;
                }
                switch (aChar) {
                    case ' ':
                        if (x == 0 || escapeSpace)
                            outBuffer.append('\\');
                        outBuffer.append(' ');
                        break;
                    case '\t':
                        outBuffer.append('\\');
                        outBuffer.append('t');
                        break;
                    case '\n':
                        outBuffer.append('\\');
                        outBuffer.append('n');
                        break;
                    case '\r':
                        outBuffer.append('\\');
                        outBuffer.append('r');
                        break;
                    case '\f':
                        outBuffer.append('\\');
                        outBuffer.append('f');
                        break;
                    case '=': // Fall through
                    case ':': // Fall through
                    case '#': // Fall through
                    case '!':
                        outBuffer.append('\\');
                        outBuffer.append(aChar);
                        break;
                    default:
                        if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                            outBuffer.append("\\u");
                            outBuffer.append(hex.toHexDigits(aChar));
                        } else {
                            outBuffer.append(aChar);
                        }
                }
            }
            return outBuffer.toString();
        }
    }
}
