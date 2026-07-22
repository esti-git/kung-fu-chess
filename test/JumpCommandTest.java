import enums.PieceColor;
import enums.PieceKind;
import model.Position;
import org.junit.jupiter.api.Test;
import protocol.JumpCommand;

import static org.junit.jupiter.api.Assertions.*;

class JumpCommandTest {

    @Test
    void testIsJumpCommand() {
        assertTrue(JumpCommand.isJumpCommand("JWNa2"));
        assertFalse(JumpCommand.isJumpCommand("WPa2a4"));
        assertFalse(JumpCommand.isJumpCommand(null));
        assertFalse(JumpCommand.isJumpCommand("J"));
    }

    @Test
    void testParseValidJump() {
        JumpCommand command = JumpCommand.parse("JWNb1", 8);

        assertNotNull(command);
        assertEquals(PieceColor.WHITE, command.color);
        assertEquals(PieceKind.KNIGHT, command.kind);
        assertEquals(new Position(7, 1), command.position);
    }

    @Test
    void testParseRejectsNonJumpPrefix() {
        assertNull(JumpCommand.parse("XWNb1", 8));
    }

    @Test
    void testParseRejectsInvalidColorOrKind() {
        assertNull(JumpCommand.parse("JXNb1", 8));
        assertNull(JumpCommand.parse("JWXb1", 8));
    }

    @Test
    void testParseRejectsInvalidSquare() {
        assertNull(JumpCommand.parse("JWNzz", 8));
    }

    @Test
    void testBuildAndParseRoundTrip() {
        String raw = JumpCommand.build(PieceColor.BLACK, PieceKind.QUEEN, 2, 3, 8);
        JumpCommand command = JumpCommand.parse(raw, 8);

        assertNotNull(command);
        assertEquals(PieceColor.BLACK, command.color);
        assertEquals(PieceKind.QUEEN, command.kind);
        assertEquals(new Position(2, 3), command.position);
    }
}
