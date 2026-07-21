package server;

/** Lifecycle of a logged-in {@link PlayerSession}. */
public enum SessionState {
    IDLE,
    SEEKING,
    PLAYING,
    DISCONNECTED_PENDING
}
