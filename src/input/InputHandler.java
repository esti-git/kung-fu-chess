package input;

import board.MatrixBoard;
import engine.GameEngine;
import io.BoardPrinter;
import model.Board;
import real_time.RealTimeUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputHandler {

    private final Board board = new MatrixBoard();
    private final BoardMapper boardMapper;
    private final Controller controller;

    public InputHandler() {
        GameEngine engine = new GameEngine(board);
        RealTimeUpdater updater = new RealTimeUpdater(board, engine);
        BoardPrinter printer = new BoardPrinter(board);
        this.boardMapper = new BoardMapper(board);
        this.controller = new Controller(engine, updater, printer);
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
