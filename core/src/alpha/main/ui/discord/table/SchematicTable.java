package alpha.main.ui.discord.table;

import static alpha.main.AlphaChan.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.data.mindustry.SchematicCache;
import alpha.main.data.mindustry.SchematicData;
import alpha.main.data.mindustry.SchematicInfo;
import alpha.main.data.mindustry.SchematicInfoCache;
import alpha.main.handler.ContentHandler;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UserHandler;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.ui.discord.PageTable;
import alpha.main.util.Log;
import alpha.main.util.StringUtils;

import mindustry.game.Schematic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class SchematicTable extends PageTable {

    private List<SchematicInfoCache> schematicInfoList = new ArrayList<>();

    private MongoCollection<SchematicData> collection;
    private SchematicInfoCache currentInfo;
    private SchematicCache currentData;
    private Message currentCode;

    public SchematicTable(SlashCommandInteractionEvent event, FindIterable<SchematicInfo> schematicInfo) {
        super(event, 10);

        MongoCursor<SchematicInfo> cursor = schematicInfo.cursor();
        while (cursor.hasNext()) {
            schematicInfoList.add(new SchematicInfoCache(cursor.next()));
        }

        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

        collection = DatabaseHandler.getCollection(Database.MINDUSTRY, schematicDataCollectionName,
                SchematicData.class);

        addButton(button("<", ButtonStyle.PRIMARY, () -> previousPage()));
        addButton(button("X", ButtonStyle.DANGER, () -> deleteTable()));
        addButton(button(">", ButtonStyle.PRIMARY, () -> nextPage()));
        addRow();
        addButton(button("like", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.LIKE.value), () -> addLike()));
        addButton(button("dislike", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.DISLIKE.value),
                () -> addDislike()));
        addButton(button("data", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.FILE.value),
                () -> sendSchematicCodeMessage()));
        addRow();
        addButton(button("delete", ButtonStyle.PRIMARY, Emoji.fromUnicode(BotConfig.TEmoji.TRASH_CAN.value),
                () -> deleteSchematic()));

    }

    @Override
    public int getMaxPage() {
        return this.schematicInfoList.size();
    }

    @Override
    public void deleteTable() {
        try {
            this.deleteSChematicCodeMessage();
            super.deleteTable();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void addLike() {
        if (currentInfo == null)
            return;

        if (currentInfo.addLike(getTriggerMember().getId()))
            updateTable();
        else
            MessageHandler.sendMessage(getEventTextChannel(), "<command.already_like>[You already like this]", 10);
    }

    private void addDislike() {
        if (currentInfo == null)
            return;

        if (currentInfo.addDislike(getTriggerMember().getId()))
            updateTable();
        else
            MessageHandler.sendMessage(getEventTextChannel(), "<command.already_dislike>[You already disliked this]",
                    10);
    }

    private void deleteSchematic() {
        if (!UserHandler.isAdmin(getTriggerMember())) {
            MessageHandler.sendMessage(getEventTextChannel(),
                    "<command.no_permission>[You have no permission to delete a schematic]", 10);
            return;
        }

        try {
            schematicInfoList.remove(currentInfo);
            currentData.delete();
            currentInfo.delete();

            if (currentCode != null)
                currentCode.delete();

            MessageHandler.sendMessage(getEventTextChannel(), "<command.schematic_deleted>[Schematic got deleted]", 10);

            if (schematicInfoList.size() == 0) {
                deleteTable();
                return;
            }

            updateTable();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void sendSchematicCodeMessage() {
        if (currentData == null)
            return;

        // BRUH
        String data = currentData.getData().getData();
        if (data == null)
            return;

        try {
            deleteSChematicCodeMessage();
            sendSchematicCodeData(data);

        } catch (Exception e) {
            Log.error(e);
        }
    }

    // Delete old code message
    private void deleteSChematicCodeMessage() {
        if (currentCode != null) {
            currentCode.delete().queue();
            currentCode = null;
        }
    }

    public void sendSchematicCodeData(String data) throws IOException {
        if (data.length() < Message.MAX_CONTENT_LENGTH) {
            getMessage().reply(data).queue(message -> this.currentCode = message);
            return;
        }

        File schematicFile = ContentHandler.getSchematicFile(ContentHandler.parseSchematic(data));
        getMessage().replyFiles(FileUpload.fromData(schematicFile)).queue(message -> this.currentCode = message);

    }

    @Override
    public void onPrepareTable(MessageEditAction action) {
        try {

            deleteSChematicCodeMessage();

            currentInfo = schematicInfoList.get(pageNumber);
            SchematicData schematicData = collection.find(Filters.eq("_id", currentInfo.getData().getId())).limit(1)
                    .first();

            if (schematicData == null) {
                MessageHandler.sendMessage(getEventTextChannel(),
                        "<command.no_schematic>[No schematic]:" + currentInfo.getData().getId(), 10);
                return;
            }

            currentData = new SchematicCache(schematicData);

            Schematic schem = ContentHandler.parseSchematic(currentData.getData().getData());
            File previewFile = ContentHandler.getSchematicPreviewFile(schem);
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder field = new StringBuilder();

            addPageFooter(builder);

            String authorId = currentInfo.getData().getAuthorId();
            if (authorId != null) {
                User user = jda.getUserById(authorId);
                Member member = getEventGuild().getMember(user);

                if (member != null) {
                    field.append("<command.author>[Author]: " + member.getEffectiveName() + "\n");
                    builder.setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(),
                            member.getEffectiveAvatarUrl());

                } else if (user != null) {
                    field.append("<command.author>[Author]: " + user.getName() + "\n");
                    builder.setAuthor(user.getName(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
                }
            }

            field.append("<command.tag>[Tag]: ");

            for (int i = 0; i < currentInfo.getData().getTag().size() - 1; i++)
                field.append(StringUtils
                        .capitalize(currentInfo.getData().getTag().get(i).replace("_", " ").toLowerCase() + ", "));

            field.append(StringUtils.capitalize(
                    currentInfo.getData().getTag().get(currentInfo.getData().getTag().size() - 1).replace("_", " ")
                            .toLowerCase() + "\n"));

            field.append("<command.like>[Like]: " + currentInfo.getLike() + "\n");
            field.append("<command.dislike>[Dislike]: " + currentInfo.getDislike() + "\n");

            builder.addField("<command.info>[Information]", field.toString(), true);

            for (Field f : ContentHandler.getSchematicInfoEmbedBuilder(schem, getEvent().getMember()).getFields()) {
                builder.addField(f);
            }

            builder = MessageHandler.getFormatEmbedBuilder(getEventGuild(), builder);

            builder.setImage("attachment://" + previewFile.getName()).setTitle(schem.name());

            action.setEmbeds(builder.build())
                    .setFiles(FileUpload.fromData(previewFile));

        } catch (Exception e) {
            Log.error(e);
        }
    }
}
