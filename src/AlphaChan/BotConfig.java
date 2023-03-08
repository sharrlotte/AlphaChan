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

    private static final String configPath = "config.properties";

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
        ;
    }

    public static Properties getProperties() {
        return prop;
    }

    public static void load() {

        try {
            File file = new File(configPath);
            if (!file.exists())
                file.createNewFile();

            InputStream input = new FileInputStream(file);
            prop = new Properties();

            prop.load(input);
            if (!prop.containsKey("Default")) {
                makeDefault();
            }

            input.close();

            Log.system("Config loaded");

        } catch (IOException io) {
            Log.error(io);
        }
    }

    public static void store() {
        try (OutputStream output = new FileOutputStream(configPath)) {

            prop.store(output, null);

        } catch (IOException io) {
            Log.error(io);
        }
    }

    public static void makeDefault() {
        try (OutputStream output = new FileOutputStream(configPath)) {

            prop = new Properties();

            setProperty("Default", 1);
            setProperty(Config.YUI_ID, "719322804549320725");

            setProperty(Config.UPDATE_PERIOD, 60 * 1000);
            setProperty(Config.GUILD_ALIVE_TIME, 20);
            setProperty(Config.USER_ALIVE_TIME, 10);
            setProperty(Config.UPDATE_LIMIT, 10);
            setProperty(Config.GUILD_COLLECTION, "GUILD_DATA");
            setProperty(Config.SCHEMATIC_INFO_COLLECTION, "SCHEMATIC_INFO");
            setProperty(Config.SCHEMATIC_DATA_COLLECTION, "SCHEMATIC_DATA");
            setProperty(Config.MAX_LOG_COUNT, 8000);
            setProperty(Config.TIME_INSERT, "_timeInserted");

            prop.store(output, "DEFAULT");

            Log.system("Config loaded");

        } catch (IOException io) {
            Log.error(io);
        }
    }

    private static void setProperty(String key, Object value) {
        prop.setProperty(key, String.valueOf(value));
    }

    private static void setProperty(Config key, Object value) {
        prop.setProperty(key.name(), String.valueOf(value));
    }

    public static String readString(String key, String def) {
        return prop.getProperty(key, def);
    }

    public static String readString(Config key, String def) {
        return prop.getProperty(key.name(), def);
    }

    public static int readInt(String key, int def) {
        return Integer.parseInt(prop.getProperty(key, String.valueOf(def)));
    }

    public static int readInt(Config key, int def) {
        return Integer.parseInt(prop.getProperty(key.name(), String.valueOf(def)));
    }

    public static float readFloat(String key, float def) {
        return Float.parseFloat(prop.getProperty(key, String.valueOf(def)));
    }

    public static float readFloat(Config key, float def) {
        return Float.parseFloat(prop.getProperty(key.name(), String.valueOf(def)));
    }

}
