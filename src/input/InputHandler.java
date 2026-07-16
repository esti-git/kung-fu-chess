package input;

import io.BoardParser;
import model.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputHandler {

    private final GameFactory factory;
    private final BoardParser boardParser;
    private final CommandRegistry registry;

    public InputHandler() {
        this.factory = new GameFactory();
        this.boardParser = factory.getBoardParser();
        this.registry = factory.getRegistry();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        List<String> rawBoardLines = new ArrayList<>();
        boolean readingBoard = false;

        System.out.println("Please paste your board configuration and commands:");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            
            // אם הגענו לסוף הקלט או שורה שמסמנת סיום, אפשר לצאת כדי למנוע תקיעה
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("end")) {
                break;
            }

            if (line.isEmpty()) continue;

            if (line.equals("Board:")) {
                readingBoard = true;
                continue;
            }

            if (line.equals("Commands:")) {
                readingBoard = false;
                List<Piece> parsedPieces = boardParser.parse(rawBoardLines);
                if (parsedPieces == null) {
                    System.out.println("ERROR: Parsing pieces failed.");
                    return;
                }

                int rows = boardParser.parseRows(rawBoardLines);
                int cols = boardParser.parseCols(rawBoardLines);
                factory.getBoard().initialize(parsedPieces, rows, cols);
                continue;
            }

            if (readingBoard) {
                rawBoardLines.add(line);
            } else {
                String[] parts = line.split(" +");
                if (parts.length > 0) {
                    registry.dispatch(parts[0], parts);
                }
                
                // פתרון מיוחד לתקיעה: אם הרצנו את פקודת ההדפסה, נעצור את הלולאה ונציג את הלוח מיידית
                if (parts.length > 0 && parts[0].equals("print")) {
                    break;
                }
            }
        }
        scanner.close();
    }
}