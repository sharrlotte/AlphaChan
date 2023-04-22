package alpha;

import org.junit.jupiter.api.Test;

import alpha.main.command.ConsoleAutoCompleteEvent;
import alpha.main.command.ConsoleCommandEvent;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleCommandRegexTest {

    @Test
    public void testConsoleCommandEventParser() {
        ConsoleCommandEvent event = ConsoleCommandEvent
                .parseCommand("show-user command=[show-guild] guildname=[{haha, 123}]");
        assertEquals("show-user", event.getCommandName());
        assertTrue(event.hasOption("command"));
        assertEquals("show-guild", event.getOption("command"));
        assertEquals("{haha, 123}", event.getOption("guildname"));

        ConsoleAutoCompleteEvent event2 = ConsoleAutoCompleteEvent.parseCommand("show-user con", 11);

        assertEquals("con", event2.getFocusString());
    }
}
