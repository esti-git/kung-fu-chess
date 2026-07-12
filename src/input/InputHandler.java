package input;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputHandler {

    private final GameFactory factory;
    private final BoardMapper boardMapper;
    private final Controller controller;

    public InputHandler() {
        this.factory = new GameFactory();
        this.boardMapper = factory.getBoardMapper();
        this.controller = factory.getController();
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
