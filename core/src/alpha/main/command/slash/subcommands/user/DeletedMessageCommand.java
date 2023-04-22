package alpha.main.command.slash.subcommands.user;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.handler.DatabaseHandler.LogCollection;
import alpha.main.ui.discord.PageTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class DeletedMessageCommand extends SlashSubcommand {

    private final int MAX_RETRIEVE = 100;
    private final int MAX_DISPLAY = 10;

    public DeletedMessageCommand() {
        super("deletedmessage", "<command.command_deleted_message>[Show list of deleted message by users]", true,
                false);
        addCommandOption(OptionType.INTEGER, "amount", "<command.amount>[Limit the number of messages, max is 100]",
                true);
        addCommandOption(OptionType.USER, "user", "<command.user_name>[Name of the message author]");
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        OptionMapping amountOption = event.getOption("amount");

        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalArgumentException("MEMBER IS NOT EXISTS");

        Document filter = new Document();
        if (userOption != null) {
            Member member = guild.getMember(userOption.getAsUser());
            if (member != null)
                filter.append("userId", member.getId());
        }
        int amount;
        if (amountOption == null)
            amount = MAX_RETRIEVE;
        else
            amount = Math.min(amountOption.getAsInt(), MAX_RETRIEVE);

        MongoCollection<Document> messageCollection = DatabaseHandler.getCollection(Database.LOG,
                LogCollection.MESSAGE.name(),
                Document.class);

        FindIterable<Document> data = DatabaseHandler
                .find(Database.LOG, LogCollection.MESSAGE_DELETED.name(), Document.class, new Document())
                .sort(new Document().append(BotConfig.readString(Config.TIME_INSERT, "_timeInsert"), -1));

        Document messageData;
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        PageTable table = new PageTable(event, 2);

        table.addButton(table.button("<<<", ButtonStyle.PRIMARY, () -> table.firstPage()));
        table.addButton(table.button("<", ButtonStyle.PRIMARY, () -> table.previousPage()));
        table.addButton(table.button("X", ButtonStyle.PRIMARY, () -> table.deleteTable()));
        table.addButton(table.button(">", ButtonStyle.PRIMARY, () -> table.nextPage()));
        table.addButton(table.button(">>>", ButtonStyle.PRIMARY, () -> table.lastPage()));

        MongoCursor<Document> cursor = data.iterator();
        int i = 0;
        while (cursor.hasNext()) {
            messageData = cursor.next();
            if (messageData.containsKey("messageId")) {
                filter.append("messageId", messageData.get("messageId"));
                Document message = messageCollection.find(filter).first();
                String guildId = getGuildId(message);
                if (!guild.getId().equals(guildId))
                    continue;

                String content = getMessage(message);
                if (content == null)
                    continue;

                if (content.length() > 1000)
                    content = content.substring(0, 999);

                field.append(content + "\n");

                if (i % MAX_DISPLAY == MAX_DISPLAY - 1) {
                    builder.addField("<command.deleted_message>[Deleted message]", field.toString(), false);
                    table.addPage(builder);
                    field = new StringBuilder();
                    builder.clear();
                }
                i += 1;

            }
            if (i >= amount)
                break;
        }
        builder.addField("<command.deleted_message>[Deleted message]", field.toString(), false);
        table.addPage(builder);
        table.sendTable();
    }

    public String getMessage(Document message) {
        if (message == null)
            return null;
        if (!message.containsKey("message"))
            return null;
        return message.get("message").toString();
    }

    public String getGuildId(Document message) {
        if (message == null)
            return null;
        if (!message.containsKey("guildId"))
            return null;
        return message.get("guildId").toString();
    }
}
