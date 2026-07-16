package common;

public class GameResult<T> {
    private final boolean isSuccess;
    private final String message;
    private final T data;

    public GameResult(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String message() {
        return message;
    }

    public T data() {
        return data;
    }

    public static <T> GameResult<T> success() {
        return new GameResult<>(true, "ok", null);
    }

    public static <T> GameResult<T> success(T data) {
        return new GameResult<>(true, "ok", data);
    }

    public static <T> GameResult<T> fail(String reason) {
        return new GameResult<>(false, reason, null);
    }
}
