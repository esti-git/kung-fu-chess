package view;

import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.GameEndedEvent;
import events.GameStartedEvent;

/**
 * Subscribes to {@link GameStartedEvent}/{@link GameEndedEvent} and tracks the game-over overlay
 * state on the UI's behalf, so rendering code never needs to ask the engine directly whether the
 * game is over or who won.
 */
public class GameAnimationController {

    private volatile boolean showGameOverOverlay;
    private volatile PieceColor winnerColor;

    public GameAnimationController(EventBus eventBus) {
        eventBus.subscribe(GameStartedEvent.TYPE, this::onGameStarted);
        eventBus.subscribe(GameEndedEvent.TYPE, this::onGameEnded);
    }

    public boolean isShowingGameOverOverlay() {
        return showGameOverOverlay;
    }

    public PieceColor getWinnerColor() {
        return winnerColor;
    }

    private void onGameStarted(Event event) {
        showGameOverOverlay = false;
        winnerColor = null;
    }

    private void onGameEnded(Event event) {
        GameEndedEvent ended = (GameEndedEvent) event;
        winnerColor = ended.getWinnerColor();
        showGameOverOverlay = true;
    }
}
