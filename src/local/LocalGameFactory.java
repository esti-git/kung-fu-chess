package local;

import engine.GameFactory;

public class LocalGameFactory extends GameFactory {

    private final LocalBoardPrinter printer;
    private final LocalBoardMapper boardMapper;
    private final LocalController controller;
    private final LocalCommandRegistry registry;

    public LocalGameFactory() {
        super();
        this.printer = new LocalBoardPrinter(eventBus);
        this.printer.setEngine(engine);

        this.boardMapper = new LocalBoardMapper(board);
        this.controller = new LocalController(engine, boardMapper, printer);
        this.registry = new LocalCommandRegistry(controller, engine, printer);

        this.printer.setRegistry(this.registry);
        this.printer.setController(this.controller);
        this.printer.setRestartAction(this::restartGame);
    }

    @Override
    public void restartGame() {
        super.restartGame();
        printer.resetTrackers();
    }

    public LocalBoardPrinter getPrinter() { return printer; }
    public LocalBoardMapper getBoardMapper() { return boardMapper; }
    public LocalController getController() { return controller; }
    public LocalCommandRegistry getRegistry() { return registry; }
}
