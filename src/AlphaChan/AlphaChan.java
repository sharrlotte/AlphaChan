package AlphaChan;

import AlphaChan.main.event.Signal;
import AlphaChan.main.handler.CommandHandler;
import AlphaChan.main.handler.ContentHandler;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.GuildHandler;
import AlphaChan.main.handler.LocaleManager;
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

    public static final Signal<Integer> onShutdown = new Signal<>();

    public AlphaChan() {
        try {

            Log.system("Bot awaking");

            BotConfig.load();

            Log.system("Connecting to discord");

            String TOKEN = System.getenv("DISCORD_BOT_TOKEN");

            jda = JDABuilder.createDefault(TOKEN, //
                    GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MODERATION, GatewayIntent.GUILD_INVITES)

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

        } catch (Exception e) {
            Log.error(e);
        }
    }

    public static void shutdown() {
        Log.system("Bot shutting down");

        try {
            onShutdown.emit(0);

        } finally {
            Log.system("Bot shutdown");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new AlphaChan();
    }
}
