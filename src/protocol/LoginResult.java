package protocol;

/** Decoded form of a "loginResult" message: whether login/registration succeeded, this
 *  client's rating on success, or an error message on failure. */
public class LoginResult {
    public final boolean success;
    public final int rating;
    public final String message;

    public LoginResult(boolean success, int rating, String message) {
        this.success = success;
        this.rating = rating;
        this.message = message;
    }
}
