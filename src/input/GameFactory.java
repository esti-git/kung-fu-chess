package input;

import board.MatrixBoard;
import engine.GameEngine;
import io.BoardParser;
import io.BoardPrinter;
import model.GameState;
import realTime.RealTimeArbiter;

public class GameFactory {
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
}

    private GameState createGameState() {
        return new GameState(board);
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
