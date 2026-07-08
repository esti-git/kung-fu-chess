import java.util.List;

public class Board {
    private Piece[][] matrix;
    private int rows;
    private int cols;

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public boolean validateAndInitialize(List<String> rawBoardLines) {
        if (rawBoardLines.isEmpty()) {
            return false;
        }

        int expectedColumns = rawBoardLines.get(0).split(" +").length;

        for (String line : rawBoardLines) {
            String[] tokens = line.split(" +");
            if (tokens.length != expectedColumns) {
                System.out.println("ERROR ROW_WIDTH_MISMATCH");
                return false;
            }

            for (String token : tokens) {
                if (token.equals(".")) continue;

                if (token.length() == 2 && isValidColor(token.charAt(0)) && isValidPiece(token.charAt(1))) {
                    continue;
                }
                System.out.println("ERROR UNKNOWN_TOKEN");
                return false;
            }
        }

        // אתחול המטריצה לאחר וידוא תקינות
        this.rows = rawBoardLines.size();
        this.cols = expectedColumns;
        this.matrix = new Piece[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] tokens = rawBoardLines.get(i).split(" +");
            for (int j = 0; j < cols; j++) {
                String token = tokens[j];
                if (token.equals(".")) {
                    matrix[i][j] = null; // משבצת ריקה מיוצגת ע"י null
                } else {
                    matrix[i][j] = new Piece(token.charAt(0), token.charAt(1));
                }
            }
        }
        return true;
    }

    public Piece getPieceAt(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return null;
        return matrix[r][c];
    }

    public void setPieceAt(int r, int c, Piece piece) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            matrix[r][c] = piece;
        }
    }

    public boolean isEmpty(int r, int c) {
        return getPieceAt(r, c) == null;
    }

    private boolean isValidColor(char color) {
        return color == 'w' || color == 'b';
    }

    private boolean isValidPiece(char piece) {
        return piece == 'R' || piece == 'N' || piece == 'B' || piece == 'Q' || piece == 'K' || piece == 'P';
    }

    public void print() {
        if (matrix == null) return;
        for (int i = 0; i < rows; i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == null) {
                    rowStr.append(".");
                } else {
                    rowStr.append(matrix[i][j].getRepresentation());
                }
                if (j < cols - 1) rowStr.append(" ");
            }
            System.out.println(rowStr.toString());
        }
    }
}