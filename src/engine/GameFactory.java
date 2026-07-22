package engine;

import board.MatrixBoard;
import events.EventBus;
import events.GameStartedEvent;
import io.BoardParser;
import model.GameState;
import model.Piece;
import realTime.RealTimeArbiter;

import java.util.Arrays;
import java.util.List;

public class GameFactory {

    private static final List<String> STANDARD_BOARD = Arrays.asList(
            "bR bN bB bQ bK bB bN bR",
            "bP bP bP bP bP bP bP bP",
            ". . . . . . . .",
            ". . . . . . . .",
            ". . . . . . . .",
            ". . . . . . . .",
            "wP wP wP wP wP wP wP wP",
            "wR wN wB wQ wK wB wN wR"
    );
    protected final MatrixBoard board;
    protected final GameState state;
    protected final EventBus eventBus;
    protected final GameEngine engine;
    protected final RealTimeArbiter arbiter;
    protected final BoardParser boardParser;

    public GameFactory() {
        this.board = new MatrixBoard();
        this.state = createGameState();
        this.eventBus = new EventBus();
        this.engine = new GameEngine(state);
        this.arbiter = new RealTimeArbiter(board, eventBus);
        this.engine.setArbiter(arbiter);
        this.boardParser = new BoardParser();
    }

    private GameState createGameState() {
        return new GameState(board);
    }

    public void initializeStandardBoard() {
        List<Piece> pieces = boardParser.parse(STANDARD_BOARD);
        board.initialize(pieces, boardParser.parseRows(STANDARD_BOARD), boardParser.parseCols(STANDARD_BOARD));
        eventBus.publish(new GameStartedEvent());
    }

    public void restartGame() {
        initializeStandardBoard();
        arbiter.reset();
        engine.setGameOver(false);
    }

    public MatrixBoard getBoard() { return board; }
    public GameState getState() { return state; }
    public EventBus getEventBus() { return eventBus; }
    public GameEngine getEngine() { return engine; }
    public RealTimeArbiter getUpdater() { return arbiter; }
    public BoardParser getBoardParser() { return boardParser; }
}
