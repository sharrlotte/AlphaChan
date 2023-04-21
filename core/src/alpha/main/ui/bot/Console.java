package alpha.main.ui.bot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import alpha.main.AlphaChan;
import alpha.main.event.Signal;
import alpha.main.util.StringUtils;

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
import javafx.application.Platform;
import javafx.event.EventHandler;
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
    private static ScrollPane outputPane = new ScrollPane(output);
    private static boolean isAutoScrollToBottom = false;

    public static Signal<String> onInputAccepted = new Signal<String>();

    public synchronized static void append(Color color, String content) {
        Text text = new Text();
        text.setStyle("-fx-fill: " + StringUtils.toHexString(color) + ";-fx-font-weight:bold;");
        text.setText(content);
        output.getChildren().addAll(text);

        if (output.getChildren().size() > 10000) {
            output.getChildren().remove(0);
        }

        if (isAutoScrollToBottom)
            outputPane.setVvalue(outputPane.getVmax());
    }

    public synchronized static void appendLine(Color color, String content) {
        append(color, content + "\n");
    }

    @Override
    public void start(Stage stage) {

        try {

            PrintStream errorStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    append(Color.RED, String.valueOf((char) b));
                }

            }, false, StandardCharsets.UTF_8);

            PrintStream printStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    append(Color.WHITE, String.valueOf((char) b));
                }

            }, false, StandardCharsets.UTF_8);

            System.setErr(errorStream);

            System.setOut(printStream);

            stage.setOpacity(0);

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

            BorderPane root = new BorderPane();

            root.setCenter(outputPane);
            root.setBottom(input);

            outputPane.setHbarPolicy(ScrollBarPolicy.NEVER);
            outputPane
                    .setOnScroll((event) -> isAutoScrollToBottom = outputPane.getVvalue() == outputPane.getVmax() ? true
                            : false);

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

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    event.consume();
                    AlphaChan.shutdown();
                }
            });

            Platform.setImplicitExit(false);

            stage.setOpacity(1);
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
