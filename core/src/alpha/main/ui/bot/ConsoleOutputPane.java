package alpha.main.ui.bot;

import alpha.main.util.StringUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ConsoleOutputPane extends ScrollPane {

    private TextFlow output;

    public ConsoleOutputPane() {

        output = new TextFlow();
        setContent(output);

        setHbarPolicy(ScrollBarPolicy.NEVER);

    }

    public void append(Color color, String content) {
        Text text = new Text();
        text.setStyle("-fx-fill: " + StringUtils.toHexString(color) + ";-fx-font-weight:bold;");
        text.setText(content);
        output.getChildren().addAll(text);

        if (output.getChildren().size() > 10000) {
            output.getChildren().remove(0);
        }

        setVvalue(1.0);
    }
}
