package AlphaChan.main.command;

public abstract class SimpleConsoleCommand {

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

    public void onCommand(ConsoleCommandEvent command) {
        runCommand(command);
    }

    public abstract void runCommand(ConsoleCommandEvent command);

    public String getDescription() {
        return description;
    }
}
