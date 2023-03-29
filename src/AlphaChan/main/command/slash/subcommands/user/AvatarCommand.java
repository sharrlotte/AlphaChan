package AlphaChan.main.command.slash.subcommands.user;

import java.util.concurrent.TimeUnit;

import AlphaChan.main.command.SlashSubcommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class AvatarCommand extends SlashSubcommand {
    public AvatarCommand() {
        super("avatar", "<@command.command_avatar>");
        this.addOption(OptionType.USER, "user", "<@command.user_name>", true);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        User user = userOption.getAsUser();
        event.getHook().sendMessage("\n" + user.getAvatarUrl()).queue(_message -> _message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

}
