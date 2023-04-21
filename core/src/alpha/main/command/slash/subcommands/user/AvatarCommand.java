package alpha.main.command.slash.subcommands.user;

import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class AvatarCommand extends SlashSubcommand {
    public AvatarCommand() {
        super("avatar", "<command.command_avatar>[Show user avatar]");
        this.addOption(OptionType.USER, "user", "<command.user_name>[User want to show avatar]", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        User user = userOption.getAsUser();
        MessageHandler.reply(event.getHook(), user.getEffectiveAvatarUrl(), 60);
    }

}
