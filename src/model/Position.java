package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Position {
    private final int row;
    private final int col;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position other = (Position) o;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public String toString() {
        return "[" + row + ", " + col + "]";
    }
}
