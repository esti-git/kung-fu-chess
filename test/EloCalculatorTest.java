import org.junit.jupiter.api.Test;
import server.EloCalculator;

import static org.junit.jupiter.api.Assertions.*;

class EloCalculatorTest {

    @Test
    void testEqualRatingsSplitEvenly() {
        int[] result = EloCalculator.computeNewRatings(1200, 1200);
        assertEquals(1216, result[0]);
        assertEquals(1184, result[1]);
    }

    @Test
    void testWinnerGainsLoserLosesSameMagnitude() {
        int[] result = EloCalculator.computeNewRatings(1200, 1200);
        int winnerGain = result[0] - 1200;
        int loserLoss = 1200 - result[1];
        assertEquals(winnerGain, loserLoss);
    }

    @Test
    void testUnderdogWinGainsMoreThanEvenMatch() {
        int[] evenMatch = EloCalculator.computeNewRatings(1200, 1200);
        int[] upset = EloCalculator.computeNewRatings(1200, 1400);

        int evenGain = evenMatch[0] - 1200;
        int upsetGain = upset[0] - 1200;

        assertTrue(upsetGain > evenGain);
    }

    @Test
    void testFavoriteWinGainsLessThanEvenMatch() {
        int[] evenMatch = EloCalculator.computeNewRatings(1200, 1200);
        int[] expectedWin = EloCalculator.computeNewRatings(1400, 1200);

        int evenGain = evenMatch[0] - 1200;
        int expectedGain = expectedWin[0] - 1400;

        assertTrue(expectedGain < evenGain);
        assertTrue(expectedGain >= 0);
    }

    @Test
    void testLoserRatingNeverGoesBelowExpectedLoss() {
        int[] result = EloCalculator.computeNewRatings(1200, 800);
        assertTrue(result[1] < 800);
    }

    @Test
    void testExtremeRatingGapClampsNear0And1() {
        int[] result = EloCalculator.computeNewRatings(3000, 100);

        assertEquals(3000, result[0]);
        assertEquals(100, result[1]);
    }
}
