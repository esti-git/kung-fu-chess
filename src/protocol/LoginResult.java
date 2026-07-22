package protocol;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResult {
    public final boolean success;
    public final int rating;
    public final String message;
    public final boolean reconnected;
}
