package AlphaChan.main.command.slash;

import AlphaChan.main.command.SimpleBotCommand;
import AlphaChan.main.command.slash.subcommands.music.PlayCommand;

public class MusicCommand extends SimpleBotCommand {

    public MusicCommand() {
        super("music", "Các lệnh liên quan đến bot nhạc");
        addSubcommands(new PlayCommand());
    }
}
