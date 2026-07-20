package events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Minimal thread-safe publish/subscribe bus. Listeners are stored per event type in a
 * CopyOnWriteArrayList so publish() can safely iterate while another thread subscribes or
 * unsubscribes concurrently.
 */
public class EventBus {

    private final Map<String, CopyOnWriteArrayList<Consumer<Event>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Event> listener) {
        listeners.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void unsubscribe(String eventType, Consumer<Event> listener) {
        List<Consumer<Event>> subscribers = listeners.get(eventType);
        if (subscribers != null) {
            subscribers.remove(listener);
        }
    }

    public void publish(Event event) {
        List<Consumer<Event>> subscribers = listeners.get(event.getType());
        if (subscribers == null) {
            return;
        }
        for (Consumer<Event> listener : subscribers) {
            listener.accept(event);
        }
    }
}
