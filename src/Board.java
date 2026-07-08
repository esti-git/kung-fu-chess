import java.util.List;
public interface Board {
    boolean validateAndInitialize(List<String> rawBoardLines);
    void print();
    Piece getPieceAt(int r, int c);
    void setPieceAt(int r, int c, Piece piece);
    boolean isEmpty(int r, int c);
    int getRows();
    int getCols();
}