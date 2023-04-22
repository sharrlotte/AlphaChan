package alpha.main.command;

import javafx.application.Platform;

public class ConsoleAutoCompleteEvent extends ConsoleCommandEvent {

    private final String focusString;

    private ConsoleAutoCompleteEvent(ConsoleCommandEvent event, String focusString) {
        super(event);
        this.focusString = focusString;
    }

    public static ConsoleAutoCompleteEvent parseCommand(String command, int caretPosition) {
        ConsoleCommandEvent event = ConsoleCommandEvent.parseCommand(command);

        if (command.length() <= 1)
            return null;

        if (caretPosition < 0 || caretPosition > command.length())
            return null;

        StringBuilder builder = new StringBuilder();
        int i = caretPosition;

        while (i > 0 && command.charAt(i) != ' ') {
            builder.insert(0, command.charAt(i));
            i--;
        }

        i = caretPosition + 1;
        while (i < command.length() && command.charAt(i) != ' ') {
            builder.append(command.charAt(i));
            i++;
        }

        return new ConsoleAutoCompleteEvent(event, builder.toString());
    }

    public String getFocusString() {
        return focusString;
    }

    public void replyChoices(String choices) {

        Platform.runLater(() -> {
            // TODO Show auto complete
        });
    }

}
