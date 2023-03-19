package AlphaChan.main.handler;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import AlphaChan.main.command.SimpleTable;
import AlphaChan.main.command.SimplePageTable;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static AlphaChan.AlphaChan.*;

public final class TableHandler extends ListenerAdapter implements Updatable {

    private static TableHandler instance = new TableHandler();
    private static ConcurrentHashMap<String, SimpleTable> tableCache = new ConcurrentHashMap<String, SimpleTable>();

    private TableHandler() {
        jda.addEventListener(this);
        UpdatableHandler.addListener(this);

        Log.system("Table handler up");
    }

    public static TableHandler getInstance() {
        if (instance == null)
            instance = new TableHandler();
        return instance;
    }

    public static void add(SimpleTable table) {
        tableCache.put(table.getId(), table);
    }

    public static void add(SimplePageTable table) {
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

    public void update() {
        Iterator<SimpleTable> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            SimpleTable table = iterator.next();
            if (!table.isAlive(1)) {
                iterator.remove();
            }
        }
    }
}
