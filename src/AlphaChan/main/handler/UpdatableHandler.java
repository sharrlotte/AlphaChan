package AlphaChan.main.handler;

import java.util.Timer;
import java.util.TimerTask;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.entities.Activity;

import static AlphaChan.AlphaChan.*;

public final class UpdatableHandler {

    private static UpdatableHandler instance = new UpdatableHandler();

    private UpdatableHandler() {
        run("UPDATE", 0, BotConfig.readInt(Config.UPDATE_PERIOD, 10), () -> update());

        Log.info("SYSTEM", "Updatable handler up");
    }

    public static UpdatableHandler getInstance() {
        if (instance == null)
            instance = new UpdatableHandler();
        return instance;
    }

    public static void update() {

        UserHandler.update();
        GuildHandler.update();
        TableHandler.update();
        ServerStatusHandler.update();
        updateStatus();
    }

    public static void updateStatus() {
        jda.getPresence().setActivity(Activity.playing("with " + GuildHandler.getActiveGuildCount() + " servers | "
                + UserHandler.getActiveUserCount() + " users"));
    }

    public static void updateCommand() {
        jda.getGuilds().forEach(guild -> guild.updateCommands().complete());
        jda.updateCommands().complete();

        CommandHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c.command).complete();
            Log.info("SYSTEM", "Added command " + c.getName());
        });
        ContextMenuHandler.getCommands().forEach(c -> {
            jda.upsertCommand(c.command).complete();
            Log.info("SYSTEM", "Added command " + c.getName());
        });
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
