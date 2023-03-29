package AlphaChan.main.command.slash.subcommands.mindustry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.data.mindustry.SchematicInfo;
import AlphaChan.main.data.mindustry.SchematicTag;
import AlphaChan.main.gui.discord.table.SchematicTable;
import AlphaChan.main.handler.DatabaseHandler;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.DatabaseHandler.Database;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static com.mongodb.client.model.Sorts.descending;

public class SearchSchematicCommand extends SlashSubcommand {

    private final String SEPARATOR = ",";

    private static List<String> tags = SchematicTag.getTags();

    public SearchSchematicCommand() {
        super("searchschematic", "<?command.command_search_schematic>", true, false);
        addOption(OptionType.STRING, "tag", "<?command.schematic_tag>", false, true);
        addOption(OptionType.USER, "user", "<?command.schematic_author>");
        addOption(OptionType.BOOLEAN, "own", "<?command.prevent_other_interact>");

    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {

        Document filter = new Document();
        OptionMapping tagOption = event.getOption("tag");
        OptionMapping ownOption = event.getOption("own");
        String[] tags = {};
        if (tagOption != null) {
            tags = tagOption.getAsString().toUpperCase().split(SEPARATOR);
            // all("tag", tags);
        }

        OptionMapping userOption = event.getOption("user");
        if (userOption != null) {
            Member member = userOption.getAsMember();
            if (member != null)
                filter.append("authorId", member.getId());
        }

        String schematicCollectionName = BotConfig.readString(Config.SCHEMATIC_INFO_COLLECTION, null);

        MongoCollection<SchematicInfo> collection = DatabaseHandler.getCollection(Database.MINDUSTRY, schematicCollectionName,
                SchematicInfo.class);

        FindIterable<SchematicInfo> schematicInfo;
        if (tags.length <= 0) {
            schematicInfo = collection.find(filter, SchematicInfo.class).sort(descending("like"));

        } else {
            schematicInfo = collection.find(Filters.and(Filters.all("tag", tags), filter), SchematicInfo.class).sort(descending("like"));
        }

        if (schematicInfo.first() == null) {
            if (tagOption == null)
                MessageHandler.reply(event, "<?command.no_schematic>", 30);
            else
                MessageHandler.reply(event, "<?command.command_search_schematic_with_tag>: " + tagOption.getAsString().toLowerCase(), 30);

        } else {

            boolean own = false;
            if (ownOption == null)
                own = false;
            else
                own = ownOption.getAsBoolean();

            SchematicTable table = new SchematicTable(event, schematicInfo);
            table.sendTable();
            if (own == true)
                table.setRequester(event.getMember().getId());
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            throw new IllegalStateException("GUILD IS NOT EXISTS");

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
    }
}
