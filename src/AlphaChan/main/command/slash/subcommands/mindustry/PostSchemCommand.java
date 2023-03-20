package AlphaChan.main.command.slash.subcommands.mindustry;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.data.mindustry.SchematicData;
import AlphaChan.main.data.mindustry.SchematicInfo;
import AlphaChan.main.data.mindustry.SchematicTag;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.NetworkHandler;
import AlphaChan.main.handler.DatabaseHandler.DATABASE;
import AlphaChan.main.util.Log;

public class PostSchemCommand extends SimpleBotSubcommand {

    private final String SEPARATOR = ",";

    private static List<String> tags = SchematicTag.getTags();

    public PostSchemCommand() {
        super("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh và đăng lên cơ sở dữ liệu", false, true);
        addOption(OptionType.ATTACHMENT, "schematicfile", "File để đăng");
        addOption(OptionType.STRING, "text", "Bản thiết kế để đăng");
        addOption(OptionType.STRING, "tag", "Gắn thẻ cho bản thiết kế", true, true);
        addOption(OptionType.BOOLEAN, "preview", "Gửi hình ảnh của bản thiết kế");
    }

    @Override
    public String getHelpString() {
        return "Chuyển tập tin bản thiết kế thành hình ảnh:\n\t<schematicfile>: Tập tin chứ bản thiết kế muốn gửi, tập tin phải có định dạng (đuôi) .msch";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        OptionMapping textOption = event.getOption("text");

        if (fileOption == null && textOption == null) {
            reply(event, "Thiếu dữ liệu bản thế kế (file/text)", MAX_OPTIONS);
            return;
        }

        OptionMapping tagOption = event.getOption("tag");
        if (tagOption == null)
            throw new IllegalArgumentException("NO OPTIONS");

        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("NO OPTIONS");

        List<String> temp = Arrays.asList(tagOption.getAsString().toUpperCase().split(SEPARATOR));
        LinkedList<String> tag = new LinkedList<String>(temp);
        Predicate<String> contain = t -> (!tags.contains(t));

        tag.removeIf(contain);

        if (tag.isEmpty()) {
            reply(event, "Bản thiết kế không hợp lệ, thiếu nhãn", 30);

        } else {

            if (fileOption != null) {

                OptionMapping previewOption = event.getOption("preview");
                Attachment a = fileOption.getAsAttachment();

                String data = NetworkHandler.downloadContent(a.getUrl());
                String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

                if (schematicDataCollectionName == null) {
                    Log.error("Bot config: SCHEMATIC_DATA_COLLECTION not exist");
                    return;
                }

                MongoCollection<SchematicData> collection = DatabaseHandler.getCollection(DATABASE.MINDUSTRY,
                        schematicDataCollectionName, SchematicData.class);

                Bson filter = new Document().append("data", data);
                FindIterable<SchematicData> result = collection.find(filter);

                if (result.first() != null) {
                    reply(event, "Bản thiết kế đã tồn tại", 10);
                    return;
                }

                String uuid = UUID.randomUUID().toString();
                new SchematicData(uuid, data).update();
                new SchematicInfo(uuid, member.getId(), tag).update();

                if (previewOption != null && previewOption.getAsBoolean() == true)
                    MessageHandler.sendSchematicPreview(event);

                reply(event, "Đăng bản thiết kế thành công", 10);
            }

            if (textOption != null) {

                OptionMapping previewOption = event.getOption("preview");

                String data = textOption.getAsString();

                String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

                if (schematicDataCollectionName == null) {
                    Log.error("Bot config: SCHEMATIC_DATA_COLLECTION not exist");
                    return;
                }

                MongoCollection<SchematicData> collection = DatabaseHandler.getCollection(DATABASE.MINDUSTRY,
                        schematicDataCollectionName, SchematicData.class);

                Bson filter = new Document().append("data", data);
                FindIterable<SchematicData> result = collection.find(filter);

                if (result.first() != null) {
                    reply(event, "Bản thiết kế đã tồn tại", 10);
                    return;
                }

                String uuid = UUID.randomUUID().toString();
                new SchematicData(uuid, data).update();
                new SchematicInfo(uuid, member.getId(), tag).update();

                if (previewOption != null && previewOption.getAsBoolean() == true)
                    MessageHandler.sendSchematicPreview(event);
            }
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        try {
            String focus = event.getFocusedOption().getName();
            if (focus.equals("tag")) {
                OptionMapping tagOption = event.getOption("tag");
                if (tagOption == null)
                    return;
                String tagValue = tagOption.getAsString().trim();
                String t = "";
                if (!tagValue.endsWith(SEPARATOR))
                    t = tagValue.substring(tagValue.lastIndexOf(SEPARATOR) + 1, tagValue.length()).trim();

                List<String> temp = new ArrayList<String>(tags);
                List<String> tag = Arrays.asList(tagValue.split(SEPARATOR));
                temp.removeAll(tag);

                List<Command.Choice> options = new ArrayList<Command.Choice>();
                int c = 0;
                for (String i : temp) {
                    if (i.startsWith(t.toUpperCase())) {
                        String value = tagValue.substring(0, tagValue.lastIndexOf(SEPARATOR) + 1) + i;
                        String display = value.toLowerCase();
                        options.add(new Command.Choice(display == null ? value : display, value));
                        c += 1;
                    }
                    if (c > MAX_OPTIONS)
                        break;
                }

                event.replyChoices(options).queue();
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
