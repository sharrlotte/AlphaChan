package AlphaChan.main.command.slash.subcommands.mindustry;

import java.util.HashMap;

import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.gui.discord.table.SchematicCalculatorTable;
import AlphaChan.main.handler.TableHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CalculatorSetBlockCommand extends SlashSubcommand {

    public CalculatorSetBlockCommand() {
        super("calculatorsetblock", "Thêm khối");
        addOption(OptionType.STRING, "block", "Tên khối muốn thêm", true, true);
        addOption(OptionType.INTEGER, "amount", "Số lượng khối muốn thêm (0) để tự động thêm, (<0) để xóa khối", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent command) {

        OptionMapping blockOption = command.getOption("block");
        OptionMapping amountOption = command.getOption("amount");

        if (blockOption == null || amountOption == null) {
            throw new IllegalArgumentException("No required option");
        }

        String block = blockOption.getAsString();
        int amount = amountOption.getAsInt();

        SchematicCalculatorTable table = (SchematicCalculatorTable) TableHandler.getTable(command.getMember().getId());

        if (table == null) {
            reply(command, "Sử dụng /mindustry calculator trước đó để có thể sử dụng lệnh này", 10);
            return;
        }

        if (table.setBlock(block, amount) == false) {
            reply(command, "Thêm thất bại, khối <" + block + "> không tồn tại", 10);
            return;
        }

        delete(command);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        HashMap<String, String> choice = new HashMap<>();

        for (String key : SchematicCalculatorTable.getBlockMap().keySet()) {
            choice.put(key, key);
        }

        sendAutoComplete(event, choice);
    }
}
