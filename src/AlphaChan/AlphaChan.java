package AlphaChan;

import AlphaChan.main.event.EventHandler;
import AlphaChan.main.event.EventType;
import AlphaChan.main.handler.CommandHandler;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.NetworkHandler;
import AlphaChan.main.handler.ServerStatusHandler;
import AlphaChan.main.handler.TableHandler;
import AlphaChan.main.handler.UpdatableHandler;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class AlphaChan {

    public static JDA jda;

    public AlphaChan() {
        try {
            BotConfig.load();

            Log.system("Bot start");

            String TOKEN = System.getenv("TOKEN");

            jda = JDABuilder.createDefault(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .disableCache(CacheFlag.VOICE_STATE)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).build();
            jda.awaitReady();

            GuildHandler.getInstance();
            UserHandler.getInstance();
            CommandHandler.getInstance();
            DatabaseHandler.getInstance();
            NetworkHandler.getInstance();
            ServerStatusHandler.getInstance();
            MessageHandler.getInstance();
            TableHandler.getInstance();
            UpdatableHandler.getInstance();

            Runtime.getRuntime().addShutdownHook(new ShutdownHook());

            EventHandler.connect(EventType.BotShutdown, (event) -> {
                BotConfig.save();
            });

            EventHandler.invoke(EventType.BotShutdown);

            Log.system("Bot online");

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private class ShutdownHook extends Thread {

        public void run() {
            EventHandler.invoke(EventType.BotShutdown);
        }
    }

    public static void main(String[] args) {
        new AlphaChan();
    }
}
