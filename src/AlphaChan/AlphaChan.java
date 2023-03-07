package AlphaChan;

import AlphaChan.main.handler.CommandHandler;
import AlphaChan.main.handler.ConsoleHandler;
import AlphaChan.main.handler.ContextMenuHandler;
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

    public static void main(String[] args) {

        try {

            Log.info("SYSTEM", "Bot start");

            String TOKEN = System.getenv("TOKEN");

            jda = JDABuilder.createDefault(TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                    .disableCache(CacheFlag.VOICE_STATE)
                    .setMemberCachePolicy(MemberCachePolicy.ALL).build();
            jda.awaitReady();

            BotConfig.load();

            GuildHandler.getInstance();
            UserHandler.getInstance();
            CommandHandler.getInstance();
            DatabaseHandler.getInstance();
            NetworkHandler.getInstance();
            ServerStatusHandler.getInstance();
            MessageHandler.getInstance();
            TableHandler.getInstance();
            ContextMenuHandler.getInstance();
            UpdatableHandler.getInstance();
            ConsoleHandler.getInstance();

            Log.info("SYSTEM", "Bot online");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
