package AlphaChan.main.handler;

import java.awt.Color;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import mindustry.net.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;
import arc.util.Strings;

import static AlphaChan.AlphaChan.*;

public final class ServerStatusHandler {

    private static final int SERVER_RELOAD_PEROID = 2 * 60 * 1000;

    private static ServerStatusHandler instance = new ServerStatusHandler();

    private static HashMap<String, HashMap<String, Message>> serverStatus = new HashMap<>();
    private static HashMap<String, String> servers = new HashMap<>();

    private ServerStatusHandler() {
        // Net net = new Net(new ArcNetProvider());

        // net.discoverServers(this::addServerIP, this::endDiscover);
    }

    public synchronized static ServerStatusHandler getInstance() {
        if (instance == null)
            instance = new ServerStatusHandler();
        return instance;
    }

    public static void update() {
    }

    public void endDiscover() {
        Log.info("SYSTE", "End discover");
    }

    public void addServerIP(Host host) {
        servers.put(host.address, null);
    }

    public static void displayServerStatus(Guild guild, MessageChannel channel, String ip) {
        UpdatableHandler.run("AUTO REFRESH SERVER", 0l, SERVER_RELOAD_PEROID, () -> sendServerStatus(guild, channel, ip));
    }

    public static void refreshServerStat(Guild guild, MessageChannel channel) {
        for (String ip : serverStatus.keySet()) {
            sendServerStatus(guild, channel, ip);
        }
    }

    private static void sendServerStatus(Guild guild, MessageChannel channel, String ip) {
        NetworkHandler.pingServer(ip, result -> {
            EmbedBuilder builder = serverStatusBuilder(ip, result);
            if (serverStatus.containsKey(guild.getId()) && serverStatus.get(guild.getId()).containsKey(ip)) {
                serverStatus.get(guild.getId()).get(ip).editMessageEmbeds(builder.build()).queue();
            } else {
                HashMap<String, Message> guildServerStatus = new HashMap<String, Message>();
                if (!serverStatus.containsKey(guild.getId()))
                    serverStatus.put(guild.getId(), guildServerStatus);

                channel.sendMessageEmbeds(builder.build()).queue(_message -> {
                    guildServerStatus.put(ip, _message);
                    serverStatus.put(guild.getId(), guildServerStatus);
                });
            }
        });
    }

    public static void reloadServer(Guild guild, MessageChannel channel) {

        MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();

        List<Message> msg = history.getRetrievedHistory();
        msg.forEach(_msg -> _msg.delete().queue());
        serverStatus.clear();
        refreshServerStat(guild, channel);
    }

    public static EmbedBuilder serverStatusBuilder(String ip, Host result) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        builder.setTitle("__" + ip.toString() + "__");
        String owner = servers.get(ip);
        if (owner != null) {
            User user = jda.getUserById(owner);
            if (user != null)
                builder.setAuthor(user.getName(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
        }
        builder.setColor(Color.CYAN);

        if (result.name != null || result.mapname != null) {
            field.append("Tên máy chủ: " + Strings.stripColors(result.name) + "\nNgười chơi: " + result.players
                    + (result.playerLimit == 0 ? "" : " \\ " + result.playerLimit) + "\nBản đồ: " + Strings.stripColors(result.mapname)
                    + "\nChế độ: "
                    + (result.modeName == null ? StringUtils.capitalize(result.mode.name()) : StringUtils.capitalize(result.modeName))
                    + "\nĐợt: " + result.wave
                    + (result.description.length() == 0 ? "" : "\nMô tả: " + Strings.stripColors(result.description)) + "\nPhiên bản: "
                    + result.version + "\nPing: " + result.ping + "ms\n");

        } else {
            field.append("Máy chủ không tồn tại hoặc ngoại tuyến\n");

        }
        builder.addField("Thông tin: ", field.toString(), true);
        builder.setFooter("Lần cập nhật cuối: " + Calendar.getInstance().getTime());
        return builder;
    }
}
