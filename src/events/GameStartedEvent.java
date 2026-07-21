package events;

public class GameStartedEvent extends Event {

    public static final String TYPE = "GAME_STARTED";

    public GameStartedEvent() {
        super(TYPE);
    }
}
