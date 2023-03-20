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
        YUI_ID,

        // Cache
        UPDATE_PERIOD, // Time between each update
        UPDATE_LIMIT,
        GUILD_ALIVE_TIME,
        USER_ALIVE_TIME,

        // Database
        GUILD_COLLECTION,
        SCHEMATIC_INFO_COLLECTION,
        SCHEMATIC_DATA_COLLECTION,
        MAX_LOG_COUNT, // Max log that database can store for each type
        TIME_INSERT, // Time field name in database

        MAX_TRACK_LENGTH, // Max length for a youtube video track that can be played in second
        ;
    }

    public static final String PLAY_EMOJI = "▶️";
    public static final String PAUSE_EMOJI = "⏸";
    public static final String STOP_EMOJI = "⏹";
    public static final String CLEAR_EMOJI = "⏯️";
    public static final String FORWARD_EMOJI = "⏩";
    public static final String FILE_EMOJI = "📁";
    public static final String STAR_EMOJI = "⭐";
    public static final String PENGUIN_EMOJI = "⏩";
    public static final String PUT_LITTER_EMOJI = "🚮";

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

            } else {
                Log.system("Config loaded");
            }

        } catch (IOException io) {
            Log.error(io);
        }
    }

    public static void save() {
        try (OutputStream output = new FileOutputStream(DEFAULT)) {

            prop.store(output, null);

        } catch (IOException io) {
            Log.error(io);
        }
    }

    private static void makeDefault() {
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {

            prop = new Properties();

            setProperty("Default", 1);
            setProperty(Config.YUI_ID, "719322804549320725");

            setProperty(Config.UPDATE_PERIOD, 60 * 1000);
            setProperty(Config.GUILD_ALIVE_TIME, 20);
            setProperty(Config.USER_ALIVE_TIME, 10);
            setProperty(Config.GUILD_COLLECTION, "GUILD_DATA");
            setProperty(Config.SCHEMATIC_INFO_COLLECTION, "SCHEMATIC_INFO");
            setProperty(Config.SCHEMATIC_DATA_COLLECTION, "SCHEMATIC_DATA");
            setProperty(Config.MAX_LOG_COUNT, 8000);
            setProperty(Config.TIME_INSERT, "_timeInserted");
            setProperty(Config.MAX_TRACK_LENGTH, "600");

            prop.store(output, "DEFAULT");

            Log.system("Config loaded");

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

    public static String readString(String key, String def) {
        return getProperties().getProperty(key, def);
    }

    public static String readString(Config key, String def) {
        return getProperties().getProperty(key.name(), def);
    }

    public static int readInt(String key, int def) {
        return Integer.parseInt(getProperties().getProperty(key, String.valueOf(def)));
    }

    public static int readInt(Config key, int def) {
        return Integer.parseInt(getProperties().getProperty(key.name(), String.valueOf(def)));
    }

    public static float readFloat(String key, float def) {
        return Float.parseFloat(getProperties().getProperty(key, String.valueOf(def)));
    }

    public static float readFloat(Config key, float def) {
        return Float.parseFloat(getProperties().getProperty(key.name(), String.valueOf(def)));
    }

}
