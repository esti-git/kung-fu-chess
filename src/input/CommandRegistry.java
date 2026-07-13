package input;

import engine.GameEngine;
import io.BoardPrinter;

public class CommandRegistry {

    private final Controller controller;
    private final GameEngine engine;
    private final BoardPrinter printer;

    public CommandRegistry(Controller controller, GameEngine engine, BoardPrinter printer) {
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