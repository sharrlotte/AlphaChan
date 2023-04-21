package alpha.main.command.slash.subcommands.mindustry;

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

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.command.SlashSubcommand;
import alpha.main.data.mindustry.SchematicCache;
import alpha.main.data.mindustry.SchematicData;
import alpha.main.data.mindustry.SchematicInfo;
import alpha.main.data.mindustry.SchematicInfoCache;
import alpha.main.data.mindustry.SchematicTag;
import alpha.main.handler.DatabaseHandler;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.NetworkHandler;
import alpha.main.handler.DatabaseHandler.Database;
import alpha.main.util.Log;

public class PostSchemCommand extends SlashSubcommand {

    private final String SEPARATOR = ",";

    private static List<String> tags = SchematicTag.getTags();

    public PostSchemCommand() {
        super("postschem", "<command.command_post_schem>[Post a schematic to bot database]", false, true);
        addOption(OptionType.STRING, "tag",
                "<command.schematic_tag>[Schematic tags, you can put multiple tag separate by a colon]", true, true);
        addOption(OptionType.ATTACHMENT, "schematicfile", "<command.schematic_file>[Schematic file to post]");
        addOption(OptionType.STRING, "text", "<command.schematic_text>[Schematic code to post]");
        addOption(OptionType.BOOLEAN, "preview", "<command.show_image>[Send a schematic preview to current channel]");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        OptionMapping textOption = event.getOption("text");

        if (fileOption == null && textOption == null) {
            MessageHandler.replyTranslate(event.getHook(),
                    "<command.no_file_or_text_schematic_provided>[No file or schematic code provided", MAX_OPTIONS);
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
            MessageHandler.replyTranslate(event.getHook(), "<command.no_tag_provided>[No tag provided]", 30);

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

                MongoCollection<SchematicData> collection = DatabaseHandler.getCollection(Database.MINDUSTRY,
                        schematicDataCollectionName,
                        SchematicData.class);

                Bson filter = new Document().append("data", data);
                FindIterable<SchematicData> result = collection.find(filter);

                if (result.first() != null) {
                    MessageHandler.replyTranslate(event.getHook(),
                            "<command.schematic_exists>[Schematic is exists]", 10);
                    return;
                }

                String uuid = UUID.randomUUID().toString();
                new SchematicCache(new SchematicData(uuid, data)).update(() -> {
                });
                new SchematicInfoCache(new SchematicInfo(uuid, member.getId(), tag)).update(() -> {
                });

                if (previewOption != null && previewOption.getAsBoolean() == true)
                    MessageHandler.sendSchematicPreview(event);

                MessageHandler.replyTranslate(event.getHook(), "<command.post_schematic_success>[Schematic posted]",
                        10);
            }

            if (textOption != null) {

                OptionMapping previewOption = event.getOption("preview");

                String data = textOption.getAsString();

                String schematicDataCollectionName = BotConfig.readString(Config.SCHEMATIC_DATA_COLLECTION, null);

                if (schematicDataCollectionName == null) {
                    Log.error("Bot config: SCHEMATIC_DATA_COLLECTION not exist");
                    return;
                }

                MongoCollection<SchematicData> collection = DatabaseHandler.getCollection(Database.MINDUSTRY,
                        schematicDataCollectionName,
                        SchematicData.class);

                Bson filter = new Document().append("data", data);
                FindIterable<SchematicData> result = collection.find(filter);

                if (result.first() != null) {
                    MessageHandler.replyTranslate(event.getHook(),
                            "<command.schematic_exists>[Schematic is exists]",
                            10);
                    return;
                }

                String uuid = UUID.randomUUID().toString();
                new SchematicCache(new SchematicData(uuid, data)).update(() -> {
                });
                new SchematicInfoCache(new SchematicInfo(uuid, member.getId(), tag)).update(() -> {
                });

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
