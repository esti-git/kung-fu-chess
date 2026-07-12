package input;

import engine.GameEngine;
import io.BoardPrinter;
import realTime.RealTimeUpdater;

public class CommandRegistry {

    private final GameEngine engine;
    private final RealTimeUpdater updater;
    private final BoardPrinter printer;

    public CommandRegistry(GameEngine engine, RealTimeUpdater updater, BoardPrinter printer) {
        this.engine = engine;
        this.updater = updater;
        this.printer = printer;
    }

    public void dispatch(String commandType, String[] parts) {
        switch (commandType) {
            case "click": handleClick(parts); break;
            case "jump":  handleJump(parts);  break;
            case "wait":  handleWait(parts);  break;
            case "print": handlePrint(parts); break;
        }
    }

    private void handleClick(String[] parts) {
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        engine.handleClick(x, y);
    }

    private void handleJump(String[] parts) {
        int jx = Integer.parseInt(parts[1]);
        int jy = Integer.parseInt(parts[2]);
        engine.handleJumpCommand(jx, jy);
    }

    private void handleWait(String[] parts) {
        int ms = Integer.parseInt(parts[1]);
        updater.advance(ms);
    }

    private void handlePrint(String[] parts) {
        if (parts.length > 1 && parts[1].equals("board")) {
            printer.print();
        }
    }
}