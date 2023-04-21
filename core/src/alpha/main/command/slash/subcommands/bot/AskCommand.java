package alpha.main.command.slash.subcommands.bot;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;

import alpha.main.BotConfig;
import alpha.main.BotConfig.Config;
import alpha.main.command.SlashSubcommand;
import alpha.main.handler.MessageHandler;
import alpha.main.handler.UpdatableHandler;
import alpha.main.util.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.TimeFormat;

public class AskCommand extends SlashSubcommand {

    private long lastTime = 0;
    private Boolean hasKey = false;
    private OpenAiService api;

    private int tries = 0;
    private int cooldown = 1000 * 60 * 2; // 2 min for every question

    public AskCommand() {
        super("ask", "<command.command_ask_bot>[Ask bot a question]");
        addOption(OptionType.STRING, "question", "<command.question>[The question]", true);

        String chatGPTKey = BotConfig.readString(Config.CHAT_GPT_TOKEN, "NULL");

        if (chatGPTKey.isBlank()) {
            throw new IllegalArgumentException("No chat gpt key found on env");
        }

        api = new OpenAiService(chatGPTKey);
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {

        long remain = cooldown + lastTime - System.currentTimeMillis();

        tries += 1;

        if (hasKey != true) {
            MessageHandler.reply(event.getHook(), "Ad chưa có tiền mua acc chat gpt nên chưa dùng được :v", 10);
            return;
        }

        if (remain > 0) {
            MessageHandler.reply(event.getHook(),
                    "Vui lòng đợi " + TimeFormat.RELATIVE.before(System.currentTimeMillis() + remain).toString() //
                            + " để sử dụng lệnh",
                    (int) remain);
            return;
        }

        OptionMapping questionOption = event.getOption("question");

        if (questionOption == null) {
            MessageHandler.reply(event.getHook(), "BRUH", 10);
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

                MessageHandler.reply(event.getHook(),
                        text + "\n\n" + api.createCompletion(request).getChoices().get(0).getText(), 1000000);

                lastTime = System.currentTimeMillis();

            } catch (Exception e) {
                if (tries < 10) {
                    runCommand(event);

                } else {
                    MessageHandler.delete(event.getHook());
                    Log.error(e);
                    tries = 0;
                }
            }
        });
    }
}
