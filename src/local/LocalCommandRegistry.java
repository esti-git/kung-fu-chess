package local;

import engine.GameEngine;

public class LocalCommandRegistry {

    private final LocalController controller;
    private final GameEngine engine;
    private final LocalBoardPrinter printer;

    public LocalCommandRegistry(LocalController controller, GameEngine engine, LocalBoardPrinter printer) {
        this.controller = controller;
        this.engine = engine;
        this.printer = printer;
    }

    public void dispatch(String commandType, String[] parts) {
        switch (commandType) {
            case "click":controller.handleRawClick(parts);break;
            case "jump":controller.handleRawJump(parts);break;
            case "wait":engine.handleRawWait(parts);break;
            case "print":printer.handleRawPrint(parts);break;
            default:System.out.println("Unknown command: " + commandType);break;
        }
    }
}
