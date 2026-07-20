package events;

/** Base class for everything published on the {@link EventBus}. */
public abstract class Event {

    private final String type;
    private final long timestamp;

    protected Event(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
