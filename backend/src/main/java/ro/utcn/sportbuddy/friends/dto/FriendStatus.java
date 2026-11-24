package ro.utcn.sportbuddy.friends.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// requestId vine doar la PENDING_SENT și PENDING_RECEIVED, ca profilul să poată anula sau accepta cererea.
// canAdd vine doar la NONE, din lista de stări (/friends/statuses): false dacă setările celuilalt
// („Cine mă poate adăuga”) nu îmi permit să-i trimit cerere.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriendStatus(Status status, Long requestId, Boolean canAdd) {

    public enum Status { NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS }

    public FriendStatus(Status status, Long requestId) {
        this(status, requestId, null);
    }

    public static FriendStatus none() { return new FriendStatus(Status.NONE, null); }
}
