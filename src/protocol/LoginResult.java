package protocol;

import lombok.AllArgsConstructor;

/** Decoded form of a "loginResult" message: whether login/registration succeeded, this
 *  client's rating on success, or an error message on failure. */
@AllArgsConstructor
public class LoginResult {
    public final boolean success;
    public final int rating;
    public final String message;
}
