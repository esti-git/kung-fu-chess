import enums.PieceColor;
import enums.PieceKind;
import model.Position;
import org.junit.jupiter.api.Test;
import protocol.MoveCommand;

import static org.junit.jupiter.api.Assertions.*;

class MoveCommandTest {

    @Test
    void testParseValidCommand() {
        MoveCommand command = MoveCommand.parse("WPa2a4", 8);

        assertNotNull(command);
        assertEquals(PieceColor.WHITE, command.color);
        assertEquals(PieceKind.PAWN, command.kind);
        assertEquals(new Position(6, 0), command.from);
        assertEquals(new Position(4, 0), command.to);
    }

    @Test
    void testParseRejectsWrongLength() {
        assertNull(MoveCommand.parse("WPa2a44", 8));
        assertNull(MoveCommand.parse("WPa2a", 8));
    }

    @Test
    void testParseRejectsNull() {
        assertNull(MoveCommand.parse(null, 8));
    }

    @Test
    void testParseRejectsInvalidColor() {
        assertNull(MoveCommand.parse("XPa2a4", 8));
    }

    @Test
    void testParseRejectsInvalidKind() {
        assertNull(MoveCommand.parse("WXa2a4", 8));
    }

    @Test
    void testParseRejectsInvalidSquare() {
        assertNull(MoveCommand.parse("WP12a4", 8));
        assertNull(MoveCommand.parse("WPa2aa", 8));
    }

    @Test
    void testSquareNameAndSquareToPositionRoundTrip() {
        Position pos = new Position(3, 5);
        String square = MoveCommand.squareName(pos.getRow(), pos.getCol(), 8);
        Position back = MoveCommand.squareToPosition(square, 8);

        assertEquals(pos, back);
    }

    @Test
    void testSquareNameFormat() {
        assertEquals("a8", MoveCommand.squareName(0, 0, 8));
        assertEquals("h1", MoveCommand.squareName(7, 7, 8));
    }
}
