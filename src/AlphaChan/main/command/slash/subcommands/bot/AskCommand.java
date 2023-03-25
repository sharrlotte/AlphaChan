package AlphaChan.main.command.slash.subcommands.bot;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import AlphaChan.main.command.SimpleBotSubcommand;
import AlphaChan.main.util.StringUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class AskCommand extends SimpleBotSubcommand {

    private long lastTime = 0;
    private String chatGPTKey;
    private int tries = 0;
    private OpenAiService api;

    private Boolean giau = false;

    private int cooldown = 1000 * 60;

    public AskCommand() {
        super("ask", "Hỏi bot");
        addOption(OptionType.STRING, "question", "Câu hỏi", true);

        chatGPTKey = System.getenv("CHAT_GPT_TOKEN");

        if (chatGPTKey.isBlank()) {
            throw new IllegalArgumentException("No chat gpt key found on env");
        }

        api = new OpenAiService(chatGPTKey);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent command) {

        long remain = System.currentTimeMillis() - lastTime;

        tries += 1;

        if (giau != true) {
            reply(command, "Ad chưa có tiền mua acc chat gpt nên chưa dùng được :v", 10);
            return;
        }

        if (remain < cooldown) {
            reply(command, "Vui lòng đợi " + StringUtils.toTime(remain) + " để sử dụng lệnh", 10);
            return;
        }

        OptionMapping questionOption = command.getOption("question");

        if (questionOption == null) {
            reply(command, "BRUH", 10);
            return;
        }
        String text = questionOption.getAsString();

        try {

            CompletionRequest request = CompletionRequest.builder().prompt(text)//
                    .model("ada")//
                    .build();//

            api.createCompletion(request).getChoices().forEach((reponse -> reply(command, reponse.toString(), 60)));

        } catch (Exception e) {
            if (tries < 10) {
                runCommand(command);

            } else {
                delete(command);
                e.printStackTrace();
                tries = 0;
            }
        }
    }
}
