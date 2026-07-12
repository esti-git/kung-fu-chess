package input;

import board.MatrixBoard;
import engine.GameEngine;
import io.BoardPrinter;
import model.GameState;
import realTime.RealTimeUpdater;

public class GameFactory {
    private final MatrixBoard board;
    private final GameState state;
    private final GameEngine engine;
    private final RealTimeUpdater updater;
    private final BoardPrinter printer;
    private final BoardMapper boardMapper;
    private final CommandRegistry registry;
    private final Controller controller;

    public GameFactory() {
        this.board = new MatrixBoard();
        this.state = createGameState();
        this.engine = new GameEngine(state);
        this.updater = new RealTimeUpdater(engine);
        this.printer = new BoardPrinter(board);
        this.boardMapper = new BoardMapper(board);
        this.registry = new CommandRegistry(engine, updater, printer);
        this.controller = new Controller(engine, registry);
    }

    private GameState createGameState() {
        GameState newState = new GameState();
        newState.setBoard(board);
        return newState;
    }

    public MatrixBoard getBoard() { return board; }
    public GameState getState() { return state; }
    public GameEngine getEngine() { return engine; }
    public RealTimeUpdater getUpdater() { return updater; }
    public BoardPrinter getPrinter() { return printer; }
    public BoardMapper getBoardMapper() { return boardMapper; }
    public CommandRegistry getRegistry() { return registry; }
    public Controller getController() { return controller; }
}
