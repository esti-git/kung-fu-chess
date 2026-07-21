package server;

public class EloCalculator {

    private static final int K_FACTOR = 32;

    public static int[] computeNewRatings(int winnerRating, int loserRating) {
        double expectedWinner = 1.0 / (1.0 + Math.pow(10, (loserRating - winnerRating) / 400.0));
        double expectedLoser = 1.0 - expectedWinner;

        int newWinner = winnerRating + (int) Math.round(K_FACTOR * (1.0 - expectedWinner));
        int newLoser = loserRating + (int) Math.round(K_FACTOR * (0.0 - expectedLoser));

        return new int[] { newWinner, newLoser };
    }
}
