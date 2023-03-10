package AlphaChan.main.event;

import java.util.HashMap;
import java.util.function.Consumer;

public class EventHandler {

    // Signals container
    private static HashMap<String, Observer<Event>> observers = new HashMap<>();

    private static final String GLOBAL = "Global ";

    @SuppressWarnings("unchecked")
    public static <T extends Event> void connect(Object source, Class<T> type, Consumer<T> callback) {
        String eventName = getSignalName(source, type);

        if (!observers.containsKey(eventName))
            observers.put(eventName, new Observer<Event>());

        Observer<T> observer = (Observer<T>) observers.get(eventName);
        observer.addListener(callback);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void connect(Class<T> type, Consumer<T> callback) {
        String eventName = getSignalName(GLOBAL, type);

        if (!observers.containsKey(eventName))
            observers.put(eventName, new Observer<Event>());

        Observer<T> observer = (Observer<T>) observers.get(eventName);
        observer.addListener(callback);
    }

    public static void connect(Event type, Consumer<Event> callback) {
        String eventName = getSignalName(GLOBAL, type.getClass());

        if (!observers.containsKey(eventName))
            observers.put(eventName, new Observer<Event>());

        Observer<Event> observer = (Observer<Event>) observers.get(eventName);
        observer.addListener(callback);
    }

    public static <T extends Event> void disconnect(Object source, Class<T> type) {
        String eventName = getSignalName(source, type);
        observers.remove(eventName);
    }

    public static <T extends Event> void disconnect(Class<T> type) {
        String eventName = getSignalName(GLOBAL, type);
        observers.remove(eventName);
    }

    public static <T extends Event> void invoke(Object source, T type) {
        Observer<Event> observer = observers.get(getSignalName(source, type));
        if (observer != null)
            observer.invoke(type);
    }

    public static <T extends Event> void invoke(T type) {
        Observer<Event> observer = observers.get(getSignalName(GLOBAL, type));

        if (observer != null)
            observer.invoke(type);
    }

    private static <T> String getSignalName(Object source, Class<T> type) {
        return source.toString() + type.getTypeName();
    }

    private static <T> String getSignalName(Object source, T type) {
        return getSignalName(source, type.getClass());

    }
}
