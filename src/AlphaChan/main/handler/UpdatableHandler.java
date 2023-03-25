package AlphaChan.main.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Activity;

import static AlphaChan.AlphaChan.*;

public final class UpdatableHandler {

    private static UpdatableHandler instance = new UpdatableHandler();
    private static List<Updatable> listeners = new ArrayList<>();

    private UpdatableHandler() {
        run("UPDATE", 0, BotConfig.readInt(Config.UPDATE_PERIOD, 10), () -> update());

        Log.system("Updatable handler up");
    }

    public static UpdatableHandler getInstance() {
        if (instance == null)
            instance = new UpdatableHandler();
        return instance;
    }

    public static void addListener(Updatable listener) {
        listeners.add(listener);
    }

    private static void update() {

        // ServerStatusHandler.update();
        try {
            for (Updatable listener : listeners)
                listener.update();

            updateStatus();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void updateStatus() {
        if (jda == null)
            return;

        jda.getPresence().setActivity(Activity
                .playing("with " + GuildHandler.getActiveGuildCount() + " servers | " + UserHandler.getActiveUserCount() + " users"));
    }

    public static void run(String name, long delay, long period, Runnable r) {
        new Timer(name, true).schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, delay, period);
    }

    public static void run(String name, long delay, Runnable r) {
        new Timer(name, true).schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, delay);
    }
}
