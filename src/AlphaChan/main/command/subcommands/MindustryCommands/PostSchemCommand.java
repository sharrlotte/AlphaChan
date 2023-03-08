package AlphaChan.main.command.subcommands.MindustryCommands;

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

import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.NetworkHandler;
import AlphaChan.main.mindustry.SchematicTag;
import AlphaChan.main.mindustry.SchematicData;
import AlphaChan.main.mindustry.SchematicInfo;
import AlphaChan.main.util.Log;
import AlphaChan.main.util.SimpleBotSubcommand;

public class PostSchemCommand extends SimpleBotSubcommand {

    private final String SEPARATOR = ",";

    private static List<String> tags = SchematicTag.getTags();

    public PostSchemCommand() {
        super("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh", false, true);
        this.addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true)//
                .addOption(OptionType.STRING, "tag", "Gắn thẻ cho bản thiết kế", true, true);
    }

    @Override
    public String getHelpString() {
        return "Chuyển tập tin bản thiết kế thành hình ảnh:\n\t<schematicfile>: Tập tin chứ bản thiết kế muốn gửi, tập tin phải có định dạng (đuôi) .msch";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        if (fileOption == null)
            throw new IllegalArgumentException("NO OPTIONS");

        OptionMapping tagOption = event.getOption("tag");
        if (tagOption == null)
            throw new IllegalArgumentException("NO OPTIONS");

        Member member = event.getMember();
        if (member == null)
            throw new IllegalStateException("NO OPTIONS");

        Attachment a = fileOption.getAsAttachment();
        String data = NetworkHandler.downloadContent(a.getUrl());

        List<String> temp = Arrays.asList(tagOption.getAsString().toUpperCase().split(SEPARATOR));
        LinkedList<String> tag = new LinkedList<String>(temp);
        Predicate<String> contain = t -> (!tags.contains(t));

        tag.removeIf(contain);

        if (tag.isEmpty()) {
            reply(event, "Bản thiết kế không hợp lệ, thiếu nhãn", 30);

        } else {
            String uuid = UUID.randomUUID().toString();
            new SchematicData(uuid, data).update();
            new SchematicInfo(uuid, member.getId(), tag).update();
            
            MessageHandler.sendSchematicPreview(event);
            reply(event, "Đăng bản thiết kế thành công", 10);
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
