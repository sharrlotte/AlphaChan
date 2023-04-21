package alpha.main;

import alpha.main.event.Signal;
import alpha.main.handler.CommandHandler;
import alpha.main.handler.ContentHandler;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.GuildHandler;
import alpha.main.handler.LocaleManager;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.NetworkHandler;
import alpha.main.handler.ServerStatusHandler;
import alpha.main.handler.TableHandler;
import alpha.main.handler.UpdatableHandler;
import alpha.main.handler.UserHandler;
import alpha.main.ui.bot.Console;
import alpha.main.util.Log;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javafx.application.Application;
import javafx.application.Platform;

public class AlphaChan {

    public static JDA jda;

    public static final Signal<Integer> onShutdown = new Signal<>();

    public AlphaChan() {
        try {
            UpdatableHandler.run("Console", 0, () -> Application.launch(Console.class));

            Log.system("Bot awaking");

            BotConfig.load();

            Log.system("Connecting to discord");

            String TOKEN = System.getenv("DISCORD_BOT_TOKEN");

            jda = JDABuilder.createDefault(TOKEN, //
                    GatewayIntent.GUILD_MESSAGES, //
                    GatewayIntent.MESSAGE_CONTENT, //
                    GatewayIntent.GUILD_MESSAGE_REACTIONS, //
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS, //
                    GatewayIntent.GUILD_MEMBERS, //
                    GatewayIntent.GUILD_PRESENCES, //
                    GatewayIntent.GUILD_VOICE_STATES, //
                    GatewayIntent.GUILD_MODERATION, //
                    GatewayIntent.GUILD_INVITES)

                    .enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.ALL).build();

            jda.awaitReady();

            Log.system("Ping: " + jda.getGatewayPing());

            LocaleManager.getInstance();
            ContentHandler.getInstance();
            GuildHandler.getInstance();
            UserHandler.getInstance();
            CommandHandler.getInstance();
            DatabaseHandler.getInstance();
            NetworkHandler.getInstance();
            ServerStatusHandler.getInstance();
            MessageHandler.getInstance();
            TableHandler.getInstance();
            UpdatableHandler.getInstance();

            Log.system("Bot online");

        } catch (

        Exception e) {
            Log.error(e);
        }
    }

    public static void shutdown() {

        try {
            Log.system("Bot shutting down");
            onShutdown.emit(0);

        } finally {

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException unused) {

                }
                Platform.exit();
                System.exit(0);
            }).start();
        }
    }

    public static void main(String[] args) {
        new AlphaChan();
    }
}
