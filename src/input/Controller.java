package input;

import engine.GameEngine;

public class Controller {

    private final GameEngine engine;
    private final CommandRegistry registry;

    public Controller(GameEngine engine, CommandRegistry registry) {
        this.engine = engine;
        this.registry = registry;
    }

    public void processCommand(String commandLine) {
        String[] parts = commandLine.split(" +");
        if (parts.length == 0) return;
        
        String commandType = parts[0];

        if (!engine.canExecuteCommand(commandType)) {
            return;
        }

        registry.dispatch(commandType, parts);
    }
}