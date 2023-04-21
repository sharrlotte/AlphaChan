package alpha;

import org.junit.jupiter.api.Test;

import alpha.main.command.ConsoleCommandEvent;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleCommandRegexTest {

    @Test
    public void testConsoleCommandEventParser() {
        assertEquals("show-user", ConsoleCommandEvent.parseCommand("show-user").getCommandName());

        ConsoleCommandEvent event = ConsoleCommandEvent
                .parseCommand("help command=[show-guild] guildname=[{haha, 123}]");
        assertEquals("help", event.getCommandName());
        assertTrue(event.hasOption("command"));
        assertEquals("show-guild", event.getOption("command"));
        assertEquals("{haha, 123}", event.getOption("guildname"));
    }
}
