package protocol;

import enums.PlayerRole;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RoomJoined {
    public final String roomId;
    public final PlayerRole role;
}
