package alpha.main.command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alpha.main.ui.bot.AutoCompleteTextField;

public class ConsoleAutoCompleteEvent extends ConsoleCommandEvent {

    private final String focusString;
    private final String focusValue;
    private final AutoCompleteTextField textField;
    public static final Pattern COMMAND_FOCUS_PATTERN = Pattern
            .compile("\s([\\w]+)=*([\\w]*|\"[\\w\s,]*\"|\\{[\\w\s,]*\\})");

    private ConsoleAutoCompleteEvent(ConsoleCommandEvent event, AutoCompleteTextField textField, String focusString,
            String focusValue) {
        super(event);
        this.focusString = focusString;
        this.textField = textField;
        this.focusValue = focusValue;
    }

    public static ConsoleAutoCompleteEvent parseCommand(AutoCompleteTextField textField) {
        String command = textField.getText();
        int caretPosition = textField.getCaretPosition();

        ConsoleCommandEvent event = ConsoleCommandEvent.parseCommand(command);
        String focusString = null;
        String focusValue = null;

        Matcher matcher = COMMAND_FOCUS_PATTERN.matcher(command);
        while (matcher.find()) {
            if (matcher.start() <= caretPosition && matcher.end() + 1 >= caretPosition) {
                focusString = matcher.group(1);
                focusValue = matcher.group(2);
                break;
            }
        }

        return new ConsoleAutoCompleteEvent(event, textField, focusString, focusValue);
    }

    public String getFocusString() {
        return focusString;
    }

    public String getFocusValue() {
        return focusValue;
    }

    public void replyChoices(String enteredText, List<AutoCompleteTextField.Choice> choices) {
        textField.replyChoices(enteredText, choices);
    }

}
