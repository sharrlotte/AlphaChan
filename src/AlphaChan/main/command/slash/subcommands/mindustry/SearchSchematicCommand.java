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
        super("searchschematic", "Tìm bản thiết kế dựa theo nhãn", true, false);
        addOption(OptionType.STRING, "tag", "Nhãn để lọc bản thiết kế", false, true);
        addOption(OptionType.USER, "user", "Tác giả của bản thiết kế");
        addOption(OptionType.BOOLEAN, "own", "Ngăn chăn người khác tương tác với bảng");

    }

    @Override
    public String getHelpString() {
        return "Tìm bản thiết kế theo nhãn, tác giả\n\t<tag>: Nhãn muốn tìm, có thể dùng nhiều nhãn, cách nhau bởi dấu phẩy\n\t<user: Tác giả của bản thiết kế>\nCác nút:\n\t<: Trang trước\n\t>: Trang sau\n\tx: Xóa tin nhắn\n\t📁: Lấy bản thiết kế\n\t⭐: Thích bản thiết kế\n\t🐧: \"Cánh cụt\" bản thiết kế\n\t🚮: Xóa bản thiết kế (admin only)";
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
            schematicInfo = collection.find(filter, SchematicInfo.class).sort(descending("star"));

        } else {
            schematicInfo = collection.find(Filters.and(Filters.all("tag", tags), filter), SchematicInfo.class).sort(descending("star"));
        }

        if (schematicInfo.first() == null) {
            if (tagOption == null)
                reply(event, "Không có dữ liệu về bản thiết kế", 30);
            else
                reply(event, "Không có dữ liệu về bản thiết kế với nhãn: " + tagOption.getAsString().toLowerCase(), 30);

        } else {

            boolean own = false;
            if (ownOption == null)
                own = false;
            else
                own = ownOption.getAsBoolean();

            SchematicTable table = new SchematicTable(event, schematicInfo);
            table.sendTable();
            if (own == true)
                table.setRequestor(event.getMember().getId());
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
