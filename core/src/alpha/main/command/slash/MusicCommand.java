package alpha.main.command.slash;

import alpha.main.command.SlashCommand;
import alpha.main.command.slash.subcommands.music.LeaveCommand;
import alpha.main.command.slash.subcommands.music.PlayCommand;
import alpha.main.command.slash.subcommands.music.SetVolumeCommand;
import alpha.main.command.slash.subcommands.music.SkipCommand;

public class MusicCommand extends SlashCommand {

    public MusicCommand() {
        super("music", "<command.command_music>[Commands for everyone]");
        addSubcommands(new PlayCommand());
        addSubcommands(new SkipCommand());
        addSubcommands(new SetVolumeCommand());
        addSubcommands(new LeaveCommand());
    }
}
