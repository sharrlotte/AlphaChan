package AlphaChan.main.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AlphaChan.main.util.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;

public class LocaleManager {

    public static final DiscordLocale DEFAULT_LOCALE = DiscordLocale.ENGLISH_US;

    private static final DiscordLocale[] SUPPORTED_LOCALE = { DiscordLocale.ENGLISH_UK, DiscordLocale.VIETNAMESE };

    private static final String LOCALE_FOLDER_PATH = "locale/";
    private static final String KEY_NOT_FOUND_STRING = "???";
    private static final Pattern KEY_PATTERN = Pattern.compile("<@([a-zA-Z]+)>");

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

    public static String format(DiscordLocale DiscordLocale, String s) {
        if (isLocaleSupported(DiscordLocale)) {
            Matcher matcher = KEY_PATTERN.matcher(s);
            while (matcher.matches()) {
                String match = matcher.group(0);
                String value = getOrDefault(DiscordLocale, match);
                s.replace(match, value);
            }
        }

        return s;
    }

    public static String getOrDefault(DiscordLocale DiscordLocale, String key) {
        Bundle bundle = bundles.get(DiscordLocale);
        if (bundle == null) {
            if (DiscordLocale == DEFAULT_LOCALE)
                throw new IllegalStateException("Default locale not found");

            bundle = new Bundle(DiscordLocale, LOCALE_FOLDER_PATH);
            bundles.put(DiscordLocale, bundle);
            return bundle.getOrDefault(key);
        }

        return bundle.getOrDefault(key);
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
                properties.load(input);

                input.close();

            } catch (IOException e) {
                Log.error(e);
            }
        }

        public String getLocaleFilePath() {
            return localeFolderPath + DiscordLocale.name() + ".properties";
        }

        public void save() {
            try (OutputStream output = new FileOutputStream(getLocaleFilePath())) {

                properties.store(output, null);

            } catch (IOException io) {
                Log.error(io);
            }
        }

        private String getOrDefault(String key) {
            String value = properties.getProperty(key);
            if (value == null) {
                if (DiscordLocale == DEFAULT_LOCALE) {
                    properties.put(key, KEY_NOT_FOUND_STRING);
                    save();
                    return KEY_NOT_FOUND_STRING;
                }

                value = LocaleManager.getOrDefault(DEFAULT_LOCALE, key);
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
    }
}
