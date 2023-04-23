package alpha.main.ui.bot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import alpha.main.AlphaChan;
import alpha.main.handler.CommandHandler.ConsoleCommandHandler;

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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Console extends Application {

    private static AutoCompleteTextField input = new AutoCompleteTextField();
    private static ConsoleOutputPane output = new ConsoleOutputPane();

    public synchronized static void append(Color color, String content) {
        output.append(color, content);
    }

    public synchronized static void appendLine(Color color, String content) {
        output.append(color, content + "\n");
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

            input.textProperty()
                    .addListener((observable, oldValue, newValue) -> ConsoleCommandHandler.onAutoComplete(input));
            input.setPrefHeight(15);
            input.onInputAccepted.connect((text) -> {
                appendLine(Color.web("#B5E4F4"), text);
                ConsoleCommandHandler.onCommand(text);
            });

            BorderPane root = new BorderPane();

            root.setCenter(output);
            root.setBottom(input);

            Scene scene = new Scene(root, 300, 250);

            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, //
                    new Stop(0, Color.web("#121212")), //
                    new Stop(1, Color.web("#2e2e32")));

            Background background = new Background(new BackgroundFill(gradient, null, null));

            output.setBackground(Background.EMPTY);
            output.setBackground(Background.EMPTY);
            output.widthProperty().addListener((l) -> {
                Node vp = output.lookup(".viewport");
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

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
