package AlphaChan.main.command.slash.subcommands.mindustry;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.gui.discord.table.SchematicCalculatorTable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CalculatorCommand extends SlashSubcommand {

    public CalculatorCommand() {
        super("calculator", "Gọi ra máy tính tỉ lệ cho bản thiết kế");
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent command) {
        SchematicCalculatorTable table = new SchematicCalculatorTable(command);
        
        table.sendTable().setRequester(command.getMember());
    }
}
