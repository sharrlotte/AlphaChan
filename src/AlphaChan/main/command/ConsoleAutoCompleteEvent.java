package AlphaChan.main.command;

import javafx.application.Platform;

public class ConsoleAutoCompleteEvent {

    private final String content;
    private final String focusString;

    public ConsoleAutoCompleteEvent(String content, String focusString) {
        this.content = content;
        this.focusString = focusString;
    }

    public String getContent() {
        return content;
    }

    public String getFocusString() {
        return focusString;
    }

    public void replyChoices(String choices) {

        Platform.runLater(() -> {
            //TODO Show auto complete
        });
    }

}
