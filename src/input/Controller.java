package input;

import engine.GameEngine;
import io.BoardPrinter;
import real_time.RealTimeUpdater;

public class Controller {

    private final GameEngine engine;
    private final RealTimeUpdater updater;
    private final BoardPrinter printer;

    public Controller(GameEngine engine, RealTimeUpdater updater, BoardPrinter printer) {
        this.engine = engine;
        this.updater = updater;
        this.printer = printer;
    }

    public void processCommand(String commandLine) {
        String[] parts = commandLine.split(" +");
        String commandType = parts[0];

        if (engine.isGameOver() && (commandType.equals("click") || commandType.equals("wait") || commandType.equals("jump"))) {
            return;
        }

        switch (commandType) {
            case "click":
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                engine.handleClick(x, y);
                break;

            case "jump":
                int jx = Integer.parseInt(parts[1]);
                int jy = Integer.parseInt(parts[2]);
                engine.handleJumpCommand(jx, jy);
                break;

            case "wait":
                int ms = Integer.parseInt(parts[1]);
                updater.advance(ms);
                break;

            case "print":
                if (parts.length > 1 && parts[1].equals("board")) {
                    printer.print();
                }
                break;
        }
    }
}
