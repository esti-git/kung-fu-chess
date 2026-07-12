package input;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import board.MatrixBoard;
import engine.GameEngine;
import io.BoardPrinter;
import model.GameState;
import realTime.RealTimeUpdater;

public class InputHandler {

    private final MatrixBoard board = new MatrixBoard();
    private final BoardMapper boardMapper;
    private final Controller controller;

    public InputHandler() {
        GameState state = new GameState();
        state.setBoard(board);
        GameEngine engine = new GameEngine(state);
        RealTimeUpdater updater = new RealTimeUpdater(engine);
        BoardPrinter printer = new BoardPrinter(board);
        
        this.boardMapper = new BoardMapper(board);
        
        CommandRegistry registry = new CommandRegistry(engine, updater, printer);
        this.controller = new Controller(engine, registry);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        List<String> rawBoardLines = new ArrayList<>();
        boolean readingBoard = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.equals("Board:")) {
                readingBoard = true;
                continue;
            }

            if (line.equals("Commands:")) {
                readingBoard = false;
                if (!boardMapper.map(rawBoardLines)) return;
                continue;
            }

            if (readingBoard) {
                rawBoardLines.add(line);
            } else {
                controller.processCommand(line);
            }
        }
        scanner.close();
    }
}
