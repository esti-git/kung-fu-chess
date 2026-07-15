package input;

import board.MatrixBoard;
import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
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
    private final MatrixBoard board;
    private final GameState state;
    private final GameEngine engine;
    private final RealTimeArbiter arbiter;
    private final BoardPrinter printer;
    private final BoardParser boardParser;
    private final BoardMapper boardMapper;
    private final CommandRegistry registry;
    private final Controller controller;

public GameFactory() {
    this.board = new MatrixBoard();
    this.state = createGameState();
    this.engine = new GameEngine(state);
    this.arbiter = new RealTimeArbiter(board);
    this.engine.setArbiter(arbiter);
    this.printer = new BoardPrinter(board);
    
    this.printer.setEngine(engine); 
    
    this.boardParser = new BoardParser();
    this.boardMapper = new BoardMapper(board);
this.controller = new Controller(engine, boardMapper, printer);
    this.registry = new CommandRegistry(controller, engine, printer);
    // החיבור החדש שמאפשר ללחיצות העכבר לשלוח פקודות:

    this.printer.setRegistry(this.registry);
    this.printer.setController(this.controller);
    this.printer.setRestartAction(this::restartGame);
}

    private GameState createGameState() {
        return new GameState(board);
    }

    /**
     * מאתחל את הלוח ישירות למערך הפתיחה הסטנדרטי, בלי צורך בהקלדת "Board:" בקונסולה בכל הרצה.
     */
    public void initializeStandardBoard() {
        List<Piece> pieces = boardParser.parse(STANDARD_BOARD);
        board.initialize(pieces, boardParser.parseRows(STANDARD_BOARD), boardParser.parseCols(STANDARD_BOARD));
    }

    /**
     * מתחיל משחק חדש מאפס על אותו לוח - נקרא על ידי BoardPrinter כמה שניות אחרי שהוצג GAME OVER.
     */
    public void restartGame() {
        initializeStandardBoard();
        arbiter.reset();
        engine.setGameOver(false);
        printer.resetTrackers();
    }

    public MatrixBoard getBoard() { return board; }
    public GameState getState() { return state; }
    public GameEngine getEngine() { return engine; }
    public RealTimeArbiter getUpdater() { return arbiter; }
    public BoardPrinter getPrinter() { return printer; }
    public BoardParser getBoardParser() { return boardParser; }
    public BoardMapper getBoardMapper() { return boardMapper; }
    public CommandRegistry getRegistry() { return registry; }
    public Controller getController() { return controller; }
}
