package AlphaChan.main.command.slash.subcommands.music;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.handler.MusicPlayerHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SkipCommand extends SimpleBotSubcommand {

    public SkipCommand() {
        super("skip", "Bỏ qua danh sách phát");
        addOption(OptionType.INTEGER, "number", "Số lượng muốn bỏ qua", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping numberOption = event.getOption("number");

        if (numberOption == null)
            return;

        MusicPlayerHandler.getInstance().getMusicPlayer(event.getGuild()).skip(numberOption.getAsInt());
        delete(event);
    }
}
