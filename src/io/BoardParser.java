package io;

import enums.PieceColor;
import enums.PieceKind;
import model.PieceModel;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class BoardParser {

    public List<PieceModel> parse(List<String> rawBoardLines) {
        if (rawBoardLines.isEmpty()) return null;

        int expectedColumns = rawBoardLines.get(0).split(" +").length;
        int idCounter = 0;

        for (String line : rawBoardLines) {
            String[] tokens = line.split(" +");
            if (tokens.length != expectedColumns) {
                System.out.println("ERROR ROW_WIDTH_MISMATCH");
                return null;
            }
            for (String token : tokens) {
                if (!token.equals(".") && !isValidToken(token)) {
                    System.out.println("ERROR UNKNOWN_TOKEN");
                    return null;
                }
            }
        }

        List<PieceModel> pieces = new ArrayList<>();
        for (int row = 0; row < rawBoardLines.size(); row++) {
            String[] tokens = rawBoardLines.get(row).split(" +");
            for (int col = 0; col < tokens.length; col++) {
                String token = tokens[col];
                if (!token.equals(".")) {
                    PieceColor color = token.charAt(0) == 'w' ? PieceColor.WHITE : PieceColor.BLACK;
                    PieceKind kind = charToKind(token.charAt(1));
                    pieces.add(new PieceModel(idCounter++, color, kind, new Position(row, col)));
                }
            }
        }
        return pieces;
    }

    public int parseRows(List<String> rawBoardLines) {
        return rawBoardLines.size();
    }

    public int parseCols(List<String> rawBoardLines) {
        return rawBoardLines.isEmpty() ? 0 : rawBoardLines.get(0).split(" +").length;
    }

    private boolean isValidToken(String token) {
        return token.length() == 2
                && (token.charAt(0) == 'w' || token.charAt(0) == 'b')
                && "RNBQKP".indexOf(token.charAt(1)) >= 0;
    }

    private PieceKind charToKind(char c) {
        switch (c) {
            case 'K': return PieceKind.KING;
            case 'Q': return PieceKind.QUEEN;
            case 'R': return PieceKind.ROOK;
            case 'B': return PieceKind.BISHOP;
            case 'N': return PieceKind.KNIGHT;
            case 'P': return PieceKind.PAWN;
            default:  return null;
        }
    }
}
