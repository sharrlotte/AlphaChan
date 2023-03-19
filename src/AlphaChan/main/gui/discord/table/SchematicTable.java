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
import AlphaChan.main.command.SimplePageTable;
import AlphaChan.main.data.mindustry.SchematicData;
import AlphaChan.main.data.mindustry.SchematicInfo;
import AlphaChan.main.handler.ContentHandler;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UserHandler;
import AlphaChan.main.handler.DatabaseHandler.DATABASE;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.StringUtils;
import mindustry.game.Schematic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import static AlphaChan.AlphaChan.*;

public class SchematicTable extends SimplePageTable {

    private List<SchematicInfo> schematicInfoList = new ArrayList<SchematicInfo>();
    private MongoCollection<SchematicData> collection;
    private SchematicInfo currentInfo;
    private SchematicData currentData;
    private Message currentCode;

    public SchematicTable(@Nonnull SlashCommandInteractionEvent event, FindIterable<SchematicInfo> schematicInfo) {
        super(event, 30);

        MongoCursor<SchematicInfo> cursor = schematicInfo.cursor();
        while (cursor.hasNext()) {
            schematicInfoList.add(cursor.next());
        }

        String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

        collection = DatabaseHandler.getCollection(DATABASE.MINDUSTRY, schematicDataCollectionName,
                SchematicData.class);

        addButton(primary("<", () -> this.previousPage()));
        addButton(deny("X", () -> this.delete()));
        addButton(primary(">", () -> this.nextPage()));
        addRow();
        addButton(primary("data", Emoji.fromMarkdown("📁"), () -> this.sendCode()));
        addButton(primary("star", Emoji.fromMarkdown("⭐"), () -> this.addStar()));
        addButton(primary("penguin", Emoji.fromMarkdown("🐧"), () -> this.addPenguin()));
        addRow();
        addButton(primary("delete", Emoji.fromMarkdown("🚮"), () -> this.deleteSchematic()));

    }

    @Override
    public int getMaxPage() {
        return this.schematicInfoList.size();
    }

    @Override
    public void delete() {
        try {
            event.getHook().deleteOriginal().queue();

            this.killTimer();

            if (currentCode != null)
                currentCode.delete().queue();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void addStar() {
        if (currentInfo == null)
            return;

        if (currentInfo.addStar(getTriggerMember().getId()))
            updateTable();
        else
            sendMessage("Bạn đã like bản thiết kế này", showPageNumber);
    }

    private void addPenguin() {
        if (currentInfo == null)
            return;

        if (currentInfo.addPenguin(getTriggerMember().getId()))
            updateTable();
        else
            sendMessage("Bạn đã dislike bản thiết kế này", showPageNumber);
    }

    private void deleteSchematic() {
        if (!UserHandler.isAdmin(getTriggerMember())) {
            sendMessage("Bạn không có quyền xóa bản thiết kế", true);
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
                delete();
                return;
            }

            updateTable();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void sendCode() {
        try {
            if (currentData == null)
                return;

            String data = currentData.data;
            if (data == null)
                return;

            if (currentCode == null)
                sendCodeData(data);
            else {
                currentCode.delete().complete();
                sendCodeData(data);
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    public void sendCodeData(@Nonnull String data) {
        if (currentData.data.length() < 1000) {
            event.getHook().sendMessage("```" + data + "```").queue(m -> this.currentCode = m);
            return;
        }

        try {
            File schematicFile = MessageHandler.getSchematicFile(ContentHandler.parseSchematic(data));
            event.getHook().sendFile(schematicFile).queue(message -> this.currentCode = message);

        } catch (IOException e) {
            Log.error(e);
        }
    }

    @Override
    public void updateTable() {
        try {
            if (currentCode != null)
                currentCode.delete().queue();

            pageNumber %= getMaxPage();

            currentInfo = schematicInfoList.get(pageNumber);
            currentData = collection.find(Filters.eq("_id", currentInfo.id)).limit(1).first();
            if (currentData == null) {
                event.getHook().editOriginal("Không có dữ liệu về bản thiết kế với id:" + currentInfo.id).queue();
                return;
            }

            Schematic schem = ContentHandler.parseSchematic(currentData.getData());
            File previewFile = MessageHandler.getSchematicPreviewFile(schem);
            EmbedBuilder builder = MessageHandler.getSchematicEmbedBuilder(schem, previewFile, event.getMember());
            StringBuilder field = new StringBuilder();
            builder = addPageFooter(builder);
            String authorId = currentInfo.authorId;
            if (authorId != null) {
                User user = jda.getUserById(authorId);
                if (user != null)
                    field.append("- Tác giả: " + user.getName() + "\n");
            }

            field.append("- Nhãn: ");
            for (int i = 0; i < currentInfo.tag.size() - 1; i++)
                field.append(StringUtils.capitalize(currentInfo.tag.get(i).replace("_", " ").toLowerCase() + ", "));

            field.append(StringUtils.capitalize(currentInfo.tag.get(
                    currentInfo.tag.size() - 1).replace("_", " ").toLowerCase() + "\n"));

            field.append("- Sao: " + currentInfo.getStar() + "\n");
            field.append("- Cánh cụt: " + currentInfo.getPenguin() + "\n");

            builder.addField("*Thông tin*", field.toString(), false);

            WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(previewFile);

            if (getTriggerMessage() != null)
                action.retainFiles(getTriggerMessage().getAttachments());

            action.setEmbeds(builder.build()).setActionRows(getButton()).queue();

        } catch (Exception e) {
            Log.error(e);
        }
    }
}
