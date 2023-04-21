package alpha.main.command;

public class ConsoleCommandOptionData {

    public enum OptionType {
        STRING(true),
        INTEGER(false),
        FLOAT(false);

        final boolean supportAutoComplete;

        private <T> OptionType(boolean supportAutoComplete) {
            this.supportAutoComplete = supportAutoComplete;
        }

        public boolean support(boolean value) {
            return value == false ? true : supportAutoComplete;
        }
    }

    private final String name;
    private final OptionType optionType;
    private final String description;
    private final boolean supportAutoComplete;
    private final boolean isRequired;

    public ConsoleCommandOptionData(String name, OptionType optionType, String description,
            boolean supportAutoComplete, boolean isRequired) {

        if (optionType.support(supportAutoComplete) == false)
            throw new IllegalArgumentException(optionType.name() + " doesn't support auto complete");

        this.name = name;
        this.optionType = optionType;
        this.description = description;
        this.supportAutoComplete = supportAutoComplete;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSupportAutoComplete() {
        return supportAutoComplete;
    }

    public boolean isRequired() {
        return isRequired;
    }

}
