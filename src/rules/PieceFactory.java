package rules;

import enums.PieceColor;
import model.Piece;
import model.Position;
import rules.pieces.Bishop;
import rules.pieces.King;
import rules.pieces.Knight;
import rules.pieces.Pawn;
import rules.pieces.Queen;
import rules.pieces.Rook;

public class PieceFactory {

public static Piece create(int id, PieceColor color, char kindChar, Position cell) {
        Piece piece;
        
        switch (kindChar) {
            case 'K': piece = new King(id, color); break;
            case 'Q': piece = new Queen(id, color); break;
            case 'R': piece = new Rook(id, color); break;
            case 'B': piece = new Bishop(id, color); break;
            case 'N': piece = new Knight(id, color); break;
            case 'P': piece = new Pawn(id, color); break;
            default:  return null;
        }
        
        piece.setCell(cell);
        return piece;
    }
}
