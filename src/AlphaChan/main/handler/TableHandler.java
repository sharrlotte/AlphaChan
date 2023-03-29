package AlphaChan.main.handler;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import AlphaChan.main.gui.discord.Table;
import AlphaChan.main.util.Log;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static AlphaChan.AlphaChan.*;

public final class TableHandler extends ListenerAdapter implements Updatable {

    private static TableHandler instance = new TableHandler();
    private static ConcurrentHashMap<String, Table> tableCache = new ConcurrentHashMap<String, Table>();

    private TableHandler() {
        jda.addEventListener(this);
        UpdatableHandler.addListener(this);

        onShutdown.connect((code) -> delete());

        Log.system("Table handler up");
    }

    public synchronized static TableHandler getInstance() {
        if (instance == null)
            instance = new TableHandler();
        return instance;
    }

    public static void addTable(Table table) {
        if (tableCache.containsKey(table.getId()))
            tableCache.get(table.getId()).deleteTable();

        tableCache.put(table.getId(), table);
    }

    public static Table getTable(String tableId) {
        return tableCache.get(tableId);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
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
        Iterator<Table> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            Table table = iterator.next();
            if (!table.isAlive(1)) {
                iterator.remove();
            }
        }
    }

    public void delete() {
        Iterator<Table> iterator = tableCache.values().iterator();
        while (iterator.hasNext()) {
            Table table = iterator.next();
            table.deleteTable();
            iterator.remove();
        }
    }
}
