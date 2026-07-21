package view;

import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.GameEndedEvent;
import events.GameStartedEvent;

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
