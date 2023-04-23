package alpha;

import org.junit.jupiter.api.Test;

import alpha.main.command.ConsoleCommandEvent;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleCommandRegexTest {

    @Test
    public void testConsoleCommandEventParser() {
        ConsoleCommandEvent event = ConsoleCommandEvent
                .parseCommand("/show-user value=good string=\"awd, awdiu, awd\" array={huhuawdh, iawhd, awidh}");

        assertEquals("show-user", event.getCommandName());
        assertEquals("good", event.getOption("value"));
        assertEquals("\"awd, awdiu, awd\"", event.getOption("string"));
        assertEquals("{huhuawdh, iawhd, awidh}", event.getOption("array"));
    }
}
