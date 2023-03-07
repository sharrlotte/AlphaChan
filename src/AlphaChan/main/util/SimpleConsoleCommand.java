package AlphaChan.main.util;

public class SimpleConsoleCommand {

    private String name = new String();
    private String description = new String();

    public SimpleConsoleCommand(String name) {
        this.name = name;
    }

    public SimpleConsoleCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void onCommand(ConsoleCommand command) {
        runCommand(command);
    }

    public void runCommand(ConsoleCommand command) {

    }

    public String getDescription() {
        return description;
    }
}
