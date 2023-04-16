package AlphaChan.main.command;

public class ConsoleCommandOptionData {

    public enum OptionType {
        STRING(true),
        INTEGER(false),
        FLOAT(false);

        final boolean supportAutoComplete;

        private <T> OptionType(boolean supportAutoComplete) {
            this.supportAutoComplete = supportAutoComplete;
        }
    }

    public final String name;
    public final OptionType optionType;
    public final String description;
    public final boolean supportAutoComplete;

    public ConsoleCommandOptionData(String name, OptionType optionType, String description,
            boolean supportAutoComplete) {
        this.name = name;
        this.optionType = optionType;
        this.description = description;
        this.supportAutoComplete = supportAutoComplete;
    }
}
