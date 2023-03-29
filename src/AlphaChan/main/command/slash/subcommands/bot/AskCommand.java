package AlphaChan.main.command.slash.subcommands.bot;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import AlphaChan.BotConfig;
import AlphaChan.BotConfig.Config;
import AlphaChan.main.command.SlashSubcommand;
import AlphaChan.main.handler.MessageHandler;
import AlphaChan.main.handler.UpdatableHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.TimeFormat;

public class AskCommand extends SlashSubcommand {

    private long lastTime = 0;
    private String chatGPTKey;
    private Boolean hasKey = false;
    private OpenAiService api;

    private int tries = 0;
    private int cooldown = 1000 * 60 * 2; // 2 min for every question

    public AskCommand() {
        super("ask", "<@command.command_ask_bot>");
        addOption(OptionType.STRING, "question", "<@command.question>", true);

        chatGPTKey = BotConfig.readString(Config.CHAT_GPT_TOKEN, "NULL");

        if (chatGPTKey.isBlank()) {
            throw new IllegalArgumentException("No chat gpt key found on env");
        }

        api = new OpenAiService(chatGPTKey);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent command) {

        long remain = cooldown + lastTime - System.currentTimeMillis();

        tries += 1;

        if (hasKey != true) {
            MessageHandler.reply(command, "Ad chưa có tiền mua acc chat gpt nên chưa dùng được :v", 10);
            return;
        }

        if (remain > 0) {
            MessageHandler.reply(command, "Vui lòng đợi " + TimeFormat.RELATIVE.before(System.currentTimeMillis() + remain).toString() //
                    + " để sử dụng lệnh", (int) remain);
            return;
        }

        OptionMapping questionOption = command.getOption("question");

        if (questionOption == null) {
            MessageHandler.reply(command, "BRUH", 10);
            return;
        }
        String text = questionOption.getAsString();

        UpdatableHandler.run("ChatGPT", 0, () -> {

            try {
                CompletionRequest request = CompletionRequest.builder().prompt(text)//
                        .model("text-davinci-003")//
                        .temperature(0.3d)//
                        .maxTokens(1000)//
                        .n(1)//
                        .build();//

                MessageHandler.reply(command, text + "\n\n" + api.createCompletion(request).getChoices().get(0).getText(), 1000000);

                lastTime = System.currentTimeMillis();

            } catch (Exception e) {
                if (tries < 10) {
                    runCommand(command);

                } else {
                    MessageHandler.delete(command);
                    e.printStackTrace();
                    tries = 0;
                }
            }
        });
    }
}
