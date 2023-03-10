package AlphaChan.main.event;

import java.util.LinkedList;
import java.util.function.Consumer;

public class Observer<T> {

    public LinkedList<Consumer<T>> callbacks = new LinkedList<>();

    public void invoke(T type) {
        for (Consumer<T> callback : callbacks) {
            callback.accept(type);
        }
    }

    public void addListener(Consumer<T> callback) {
        if (callbacks.contains(callback))
            return;
        callbacks.add(callback);
    }
}
