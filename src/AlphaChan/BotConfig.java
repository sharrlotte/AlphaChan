package AlphaChan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import AlphaChan.main.util.Log;

public final class BotConfig {

    private static final String CONFIG_PATH = "config.properties";
    private static final String DEFAULT = "DEFAULT";

    private static Properties prop = new Properties();

    public static enum Config {
        YUI_ID("719322804549320725"),

        // Cache
        UPDATE_PERIOD(60 * 1000), // Time between each update
        UPDATE_LIMIT(1), GUILD_ALIVE_TIME(30), USER_ALIVE_TIME(10),

        // Database
        GUILD_COLLECTION("GUILD_DATA"), SCHEMATIC_INFO_COLLECTION("SCHEMATIC_INFO"), SCHEMATIC_DATA_COLLECTION("SCHEMATIC_DATA"),
        MAX_LOG_COUNT(8000), // Max log messages that database can store for each type
        TIME_INSERT("time"), // Time field name in Database

        MAX_TRACK_LENGTH(600), // Max length for a youtube video track that can be played in second

        CHAT_GPT_TOKEN("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");

        public final String value;

        Config(Object value) {
            this.value = value.toString();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static enum TEmoji {
        PLAY("▶️"), //
        PAUSE("⏸"), //
        STOP("⏹"), //
        CLEAR("⏯️"), //
        FORWARD("⏩"), //
        FILE("📁"), //
        LIKE("👍"), //
        DISLIKE("👎"), //
        TRASH_CAN("🚮");

        public final String value;

        TEmoji(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static Properties getProperties() {
        if (prop == null || prop.isEmpty())
            makeDefault();

        return prop;
    }

    public static void clearProperties() {
        prop.clear();
    }

    public static void load() {

        try {
            File file = new File(CONFIG_PATH);
            if (!file.exists())
                file.createNewFile();

            InputStream input = new FileInputStream(file);
            prop = new Properties();

            prop.load(input);
            if (!prop.containsKey(DEFAULT)) {
                input.close();
                makeDefault();

            }

            Log.system("Config loaded");

        } catch (IOException io) {
            Log.error(io);
        }
    }

    public static void save() {
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {

            prop.store(output, null);

        } catch (IOException io) {
            Log.error(io);
        }
    }

    private static void makeDefault() {
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {

            prop = new Properties();

            for (Config config : Config.values()) {
                setProperty(config, config.value);
            }
            setProperty(DEFAULT, 1);

            prop.store(output, "DEFAULT");

        } catch (IOException io) {
            Log.error(io);
        }
    }

    public static boolean hasProperty(String key) {
        return getProperties().containsKey(key);
    }

    public static void setProperty(String key, Object value) {
        prop.put(key, String.valueOf(value));
    }

    public static void setProperty(Config key, Object value) {
        setProperty(key.name(), String.valueOf(value));
    }

    public static String getProperty(Config key, Object def) {

        if (hasProperty(key.name()))
            return getProperties().getProperty(key.name());

        if (def == null)
            return key.value;

        return null;
    }

    public static String getProperty(String key, Object def) {
        return getProperties().getProperty(key, def.toString());
    }

    public static String readString(String key, String def) {
        return getProperties().getProperty(key, def);
    }

    public static String readString(Config key, String def) {
        return getProperties().getProperty(key.name(), def);
    }

    public static int readInt(String key, int def) {
        return Integer.parseInt(getProperty(key, key));
    }

    public static int readInt(Config key, int def) {
        return Integer.parseInt(getProperty(key, def));
    }

    public static float readFloat(String key, float def) {
        return Float.parseFloat(getProperty(key, def));
    }

    public static float readFloat(Config key, float def) {
        return Float.parseFloat(getProperty(key, def));
    }

}
