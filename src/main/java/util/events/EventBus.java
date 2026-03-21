package util.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus {

    private static final List<Consumer<Object>> listeners = new ArrayList<>();

    public static void subscribe(Consumer<Object> listener) {
        listeners.add(listener);
    }

    public static void publish(Object event) {
        for (Consumer<Object> listener : listeners) {
            listener.accept(event);
        }
    }
}
