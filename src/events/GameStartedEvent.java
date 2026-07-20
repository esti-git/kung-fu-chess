package events;

/** Published when a new game begins (initial start or restart after game over). */
public class GameStartedEvent extends Event {

    public static final String TYPE = "GAME_STARTED";

    public GameStartedEvent() {
        super(TYPE);
    }
}
