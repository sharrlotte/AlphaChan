package alpha.main.ui.bot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import alpha.main.event.Signal;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.PopupWindow.AnchorLocation;

public class AutoCompleteTextField extends TextField {

    private List<Choice> entries;
    private ContextMenu entriesPopup;
    private String enteredText;
    private InputCache inputCache;

    public Signal<String> onInputAccepted = new Signal<String>();

    public AutoCompleteTextField() {

        entries = new ArrayList<>();
        entriesPopup = new ContextMenu();
        entriesPopup.setAutoFix(false);
        inputCache = new InputCache();

        setListener();
    }

    private void setListener() {

        textProperty().addListener((observable, oldValue, newValue) -> {
            if (enteredText == null || enteredText.isEmpty() || entries.size() == 0) {
                entriesPopup.hide();
            }
        });

        backgroundProperty().addListener((observableValue, oldValue, newValue) -> entriesPopup
                .setStyle(" -fx-background-color: #121212;-fx-border-color: #2e2e39;-fx-text-fill: white;"));

        focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue)
                entriesPopup.hide();
        });

        setOnKeyPressed((event) -> {

            if (getText() != null || !getText().isBlank())
                return;

            switch (event.getCode()) {
                case UP: {
                    String text = inputCache.getLast();
                    setText(text);
                    positionCaret(text.length());
                    break;
                }
                case DOWN: {
                    String text = inputCache.getNext();
                    setText(text);
                    positionCaret(text.length());
                    break;
                }

                default: {
                    break;
                }
            }
        });

        setOnAction((action) -> {
            String text = getText();
            inputCache.add(text);
            onInputAccepted.emit(text);
            setText(new String());
        });

    }

    private void populatePopup(List<Choice> choice, String enteredText) {

        List<CustomMenuItem> menuItems = new LinkedList<>();

        int maxEntries = 6;
        int count = Math.min(choice.size(), maxEntries);

        for (int i = 0; i < count; i++) {
            final Choice result = choice.get(i);

            Label entryLabel = new Label();
            entryLabel.setGraphic(buildTextFlow(result.name, enteredText));
            entryLabel.setPrefHeight(10);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            menuItems.add(item);

            item.setOnAction(actionEvent -> {
                if (enteredText == null || enteredText.isEmpty()) {
                    setText(getText() + result.value);

                } else {
                    int lastIndex = getText().lastIndexOf(enteredText);
                    setText(getText().substring(0, lastIndex) + result.value);
                }
                positionCaret(getText().length());
                entriesPopup.hide();
            });
        }

        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);
    }

    public void replyChoices(String enteredText, List<Choice> entries) {
        this.enteredText = enteredText;
        this.entries = entries;

        if (entries == null || entries.isEmpty()) {
            entriesPopup.hide();
            return;
        }

        entriesPopup.show(this, Side.TOP, 0, 0);
        entriesPopup.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
        populatePopup(entries, enteredText);
    }

    public void hidePopup() {
        entriesPopup.hide();
    }

    private static TextFlow buildTextFlow(String text, String filter) {

        if (filter == null)
            return buildTextFlow(text);

        int filterIndex = text.indexOf(filter);

        if (filterIndex == -1)
            return buildTextFlow(text);

        Text textBefore = new Text(text.substring(0, filterIndex));
        Text textAfter = new Text(text.substring(filterIndex + filter.length()));
        Text textFilter = new Text(text.substring(filterIndex, filterIndex + filter.length()));

        textBefore.setFill(Color.WHITE);
        textAfter.setFill(Color.WHITE);
        textFilter.setFill(Color.CYAN);
        textFilter.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));
        return new TextFlow(textBefore, textFilter, textAfter);
    }

    private static TextFlow buildTextFlow(String text) {
        Text textFill = new Text(text);
        textFill.setFill(Color.WHITE);
        return new TextFlow(textFill);
    }

    public static class Choice {

        public final String name;
        public final String value;

        public Choice(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static class InputCache {

        private ArrayList<String> cache = new ArrayList<>();
        private long cacheLimit = 100;
        private int index = 0;

        public synchronized String getNext() {
            if (cache.size() == 0)
                return "";

            if (index < 0)
                index = cache.size() - 1;

            if (index > cache.size() - 1)
                index = 0;

            return cache.get(index++);
        }

        public synchronized String getLast() {
            if (cache.size() == 0)
                return "";

            if (index < 0)
                index = cache.size() - 1;

            if (index > cache.size() - 1)
                index = 0;

            return cache.get(index--);
        }

        public synchronized void add(String content) {
            cache.add(content);
            if (cache.size() > cacheLimit)
                cache.remove(0);
        }
    }
}
