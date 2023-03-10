package AlphaChan.main.command.slash.subcommands.yui;

import AlphaChan.main.command.SimpleBotSubcommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SetRoleCommand extends SimpleBotSubcommand {

    public SetRoleCommand() {
        super("setrole", "Yui only");
        this.addOption(OptionType.ROLE, "role", "Yui only", true).//
                addOption(OptionType.USER, "user", "Yui only", true);
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            return;
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        Role role = roleOption.getAsRole();
        User user = userOption.getAsUser();
        Member member = guild.getMember(user);
        if (member == null) {
            return;
        }
        for (Role r : member.getRoles()) {
            if (role.getId().equals(r.getId())) {
                guild.addRoleToMember(user, role).queue();
                return;
            }
        }
        guild.removeRoleFromMember(user, role);
    }
}
