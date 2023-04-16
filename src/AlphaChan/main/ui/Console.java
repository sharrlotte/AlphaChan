package AlphaChan.main.ui;

import java.util.LinkedList;

import AlphaChan.AlphaChan;
import AlphaChan.main.event.Signal;
import AlphaChan.main.util.StringUtils;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Console extends Application {

    private static TextFlow output = new TextFlow();
    private static TextField input = new TextField();
    private static InputCache inputCache = new InputCache();

    public static Signal<String> onInputAccepted = new Signal<String>();

    public synchronized static void append(Color color, String content) {
        Text text = new Text();
        text.setStyle("-fx-fill: " + StringUtils.toHexString(color) + ";-fx-font-weight:bold;");
        text.setText(content);
        output.getChildren().addAll(text);
    }

    public synchronized static void appendLine(Color color, String content) {
        append(color, content + "\n");
    }

    @Override
    public void start(Stage stage) {

        try {

            output.setLineSpacing(0);

            input.setPrefHeight(15);

            input.setOnKeyPressed((key) -> {
                if (key.getCode() == KeyCode.UP) {
                    String text = inputCache.getLast();
                    input.setText(text);
                    input.positionCaret(text.length());
                }

                else if (key.getCode() == KeyCode.DOWN) {
                    String text = inputCache.getNext();
                    input.setText(text);
                    input.positionCaret(text.length());
                }
            });

            input.setOnAction((action) -> {
                String text = input.getText();
                input.setText("");
                inputCache.add(text);
                onInputAccepted.emit(text);
                appendLine(Color.web("#B5E4F4"), text);
            });

            ScrollPane outputPane = new ScrollPane(output);
            BorderPane root = new BorderPane();

            root.setCenter(outputPane);
            root.setBottom(input);

            outputPane.setHbarPolicy(ScrollBarPolicy.NEVER);

            Scene scene = new Scene(root, 300, 250);

            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, //
                    new Stop(0, Color.web("#121212")), //
                    new Stop(1, Color.web("#2e2e32")));

            Background background = new Background(new BackgroundFill(gradient, null, null));

            output.setBackground(Background.EMPTY);
            outputPane.setBackground(Background.EMPTY);
            outputPane.widthProperty().addListener((l) -> {
                Node vp = outputPane.lookup(".viewport");
                vp.setStyle("-fx-background-color:transparent;");
            });

            Border border = new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, null));

            input.setBackground(Background.EMPTY);
            input.setBorder(border);
            input.setBlendMode(BlendMode.ADD);
            input.setStyle("-fx-text-fill: white;");

            root.setBackground(background);

            stage.setTitle("Console");
            stage.setScene(scene);

            stage.setMinHeight(800);
            stage.setMinWidth(1200);

            stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST,
                    (event) -> AlphaChan.shutdown());

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class InputCache {

        private LinkedList<String> cache = new LinkedList<>();
        private long cacheLimit = 100;
        private int index = 0;

        public synchronized String getNext() {
            if (cache.size() == 0)
                return "";

            if (index > cache.size() - 1)
                index = 0;

            return cache.get(index++);
        }

        public synchronized String getLast() {
            if (cache.size() == 0)
                return "";

            if (index < 0)
                index = cache.size() - 1;

            return cache.get(index--);
        }

        public synchronized void add(String content) {
            cache.add(content);
            if (cache.size() > cacheLimit)
                cache.remove(0);
        }
    }
}
