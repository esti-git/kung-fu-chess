package server;

import enums.PieceColor;

public class RatingService {

    private final PlayerRepository repository;

    public RatingService(PlayerRepository repository) {
        this.repository = repository;
    }

    public void applyGameEnd(PieceColor winnerColor, PlayerSession whiteSession, PlayerSession blackSession) {
        PlayerSession winner = winnerColor == PieceColor.WHITE ? whiteSession : blackSession;
        PlayerSession loser = winnerColor == PieceColor.WHITE ? blackSession : whiteSession;

        int[] updated = EloCalculator.computeNewRatings(winner.getRating(), loser.getRating());
        winner.setRating(updated[0]);
        loser.setRating(updated[1]);

        repository.updateRating(winner.getUsername(), winner.getRating());
        repository.updateRating(loser.getUsername(), loser.getRating());
    }
}
