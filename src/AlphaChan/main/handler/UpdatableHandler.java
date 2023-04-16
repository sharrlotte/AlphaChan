package AlphaChan.main.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
    private static LinkedList<Runnable> cacheCleaners = new LinkedList<>();

    private UpdatableHandler() {
        run("UPDATE", 0, BotConfig.readInt(Config.UPDATE_PERIOD, 10), () -> update());
    }

    public synchronized static UpdatableHandler getInstance() {
        if (instance == null)
            instance = new UpdatableHandler();
        return instance;
    }

    public static void addListener(Updatable listener) {
        listeners.add(listener);
    }

    public static void addCacheCleaner(Runnable cleaner) {
        cacheCleaners.add(cleaner);
    }

    private static void update() {

        // ServerStatusHandler.update();
        try {
            for (Updatable listener : listeners)
                listener.update();

            // Clean cache
            Iterator<Runnable> it = cacheCleaners.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                r.run();
                it.remove();
            }

            updateStatus();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void updateStatus() {
        if (jda == null)
            return;

        jda.getPresence().setActivity(Activity
                .playing("with " + GuildHandler.getActiveGuildCount() + " servers | " + UserHandler.getActiveUserCount()
                        + " users"));
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
