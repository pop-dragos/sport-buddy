package ro.utcn.sportbuddy.friends.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// requestId vine doar la PENDING_SENT și PENDING_RECEIVED, ca profilul să poată anula sau accepta cererea.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriendStatus(Status status, Long requestId) {

    public enum Status { NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS }

    public static FriendStatus none() { return new FriendStatus(Status.NONE, null); }
}
