package AlphaChan.main.command.slash;

import AlphaChan.main.command.SlashCommand;
import AlphaChan.main.command.slash.subcommands.music.LeaveCommand;
import AlphaChan.main.command.slash.subcommands.music.PlayCommand;
import AlphaChan.main.command.slash.subcommands.music.SetVolumeCommand;
import AlphaChan.main.command.slash.subcommands.music.SkipCommand;

public class MusicCommand extends SlashCommand {

    public MusicCommand() {
        super("music", "<?command.command_music>");
        addSubcommands(new PlayCommand());
        addSubcommands(new SkipCommand());
        addSubcommands(new SetVolumeCommand());
        addSubcommands(new LeaveCommand());
    }
}
