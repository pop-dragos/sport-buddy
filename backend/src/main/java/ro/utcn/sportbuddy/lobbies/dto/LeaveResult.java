package ro.utcn.sportbuddy.lobbies.dto;

// lateCancellation = a ieșit după confirmare, deci primește penalizare
public record LeaveResult(boolean lateCancellation) {}
