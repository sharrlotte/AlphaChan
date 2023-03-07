package AlphaChan.main.handler;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import AlphaChan.main.util.SimpleEmbed;
import AlphaChan.main.util.SimpleTable;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static AlphaChan.AlphaChan.*;

public final class TableHandler extends ListenerAdapter {

    private static TableHandler instance = new TableHandler();
    private static ConcurrentHashMap<String, SimpleEmbed> tableCache = new ConcurrentHashMap<String, SimpleEmbed>();

    private TableHandler() {
        jda.addEventListener(this);
        Log.info("SYSTEM", "Table handler up");
    }

    public static TableHandler getInstance() {
        if (instance == null)
            instance = new TableHandler();
        return instance;
    }

    public static void add(SimpleEmbed table) {
        tableCache.put(table.getId(), table);
    }

    public static void add(SimpleTable table) {
        tableCache.put(table.getId(), table);
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String component = event.getComponentId();

        String[] id = component.split(":", 2);
        if (id.length < 2)
            throw new IllegalArgumentException("Invalid component id");

        if (tableCache.containsKey(id[0])) {
            tableCache.get(id[0]).onCommand(event);
        }
    }

    public static void update() {
        Iterator<SimpleEmbed> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            SimpleEmbed table = iterator.next();
            if (!table.isAlive(1)) {
                table.delete();
                iterator.remove();
            }
        }
    }
}
