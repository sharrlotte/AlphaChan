package AlphaChan.main.gui.discord.table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.data.mindustry.SchematicCache;
import AlphaChan.main.data.mindustry.SchematicData;
import AlphaChan.main.data.mindustry.SchematicInfo;
import AlphaChan.main.data.mindustry.SchematicInfoCache;
import AlphaChan.main.gui.discord.PageTable;
import AlphaChan.main.handler.ContentHandler;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;

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

import static AlphaChan.AlphaChan.*;

public class SchematicTable extends PageTable {

    private List<SchematicInfoCache> schematicInfoList = new ArrayList<>();

    private MongoCollection<SchematicData> collection;
    private SchematicInfoCache currentInfo;
    private SchematicCache currentData;
    private Message currentCode;

    public SchematicTable(@Nonnull SlashCommandInteractionEvent event, FindIterable<SchematicInfo> schematicInfo) {
        super(event, 10);

        MongoCursor<SchematicInfo> cursor = schematicInfo.cursor();
        while (cursor.hasNext()) {
            schematicInfoList.add(new SchematicInfoCache(cursor.next()));
        }

        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

        collection = DatabaseHandler.getCollection(Database.MINDUSTRY, schematicDataCollectionName, SchematicData.class);

        addButton(primary("<", () -> this.previousPage()));
        addButton(deny("X", () -> this.deleteTable()));
        addButton(primary(">", () -> this.nextPage()));
        addRow();
        addButton(primary("like", Emoji.fromUnicode(BotConfig.TEmoji.LIKE.value), () -> this.addLike()));
        addButton(primary("dislike", Emoji.fromUnicode(BotConfig.TEmoji.DISLIKE.value), () -> this.addDislike()));
        addButton(primary("data", Emoji.fromUnicode(BotConfig.TEmoji.FILE.value), () -> this.sendSchematicCodeMessage()));
        addRow();
        addButton(primary("delete", Emoji.fromUnicode(BotConfig.TEmoji.TRASH_CAN.value), () -> this.deleteSchematic()));

    }

    @Override
    public int getMaxPage() {
        return this.schematicInfoList.size();
    }

    @Override
    public void deleteTable() {
        try {
            super.deleteTable();
            this.kill();
            this.deleteSChematicCodeMessage();

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
            sendMessage("Bạn đã like bản thiết kế này", 10);
    }

    private void addDislike() {
        if (currentInfo == null)
            return;

        if (currentInfo.addDislike(getTriggerMember().getId()))
            updateTable();
        else
            sendMessage("Bạn đã dislike bản thiết kế này", 10);
    }

    private void deleteSchematic() {
        if (!UserHandler.isAdmin(getTriggerMember())) {
            sendMessage("Bạn không có quyền xóa bản thiết kế", 10);
            return;
        }

        try {
            schematicInfoList.remove(currentInfo);
            currentData.delete();
            currentInfo.delete();

            if (currentCode != null)
                currentCode.delete();

            sendMessage("Đã xóa bản thiết kế", 10);

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
            currentCode.delete().complete();
            currentCode = null;
        }
    }

    public void sendSchematicCodeData(@Nonnull String data) throws IOException {
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
            SchematicData schematicData = collection.find(Filters.eq("_id", currentInfo.getData().getId())).limit(1).first();

            if (schematicData == null) {
                sendMessage("Không có dữ liệu về bản thiết kế với id:" + currentInfo.getData().getId(), 10);
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
                    field.append("- Tác giả: " + member.getEffectiveName() + "\n");
                    builder.setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl());

                } else if (user != null) {
                    field.append("- Tác giả: " + user.getName() + "\n");
                    builder.setAuthor(user.getName(), user.getEffectiveAvatarUrl(), user.getEffectiveAvatarUrl());
                }
            }

            field.append("- Nhãn: ");

            for (int i = 0; i < currentInfo.getData().getTag().size() - 1; i++)
                field.append(StringUtils.capitalize(currentInfo.getData().getTag().get(i).replace("_", " ").toLowerCase() + ", "));

            field.append(StringUtils.capitalize(
                    currentInfo.getData().getTag().get(currentInfo.getData().getTag().size() - 1).replace("_", " ").toLowerCase() + "\n"));

            field.append("- Like: " + currentInfo.getLike() + "\n");
            field.append("- Dislike: " + currentInfo.getDislike() + "\n");

            builder.addField("Thông tin", field.toString(), true);

            for (Field f : ContentHandler.getSchematicInfoEmbedBuilder(schem, getEvent().getMember()).getFields()) {
                builder.addField(f);
            }

            builder.setImage("attachment://" + previewFile.getName()).setTitle(schem.name());

            action.setEmbeds(builder.build()).setFiles(FileUpload.fromData(previewFile));

        } catch (Exception e) {
            Log.error(e);
        }
    }
}
