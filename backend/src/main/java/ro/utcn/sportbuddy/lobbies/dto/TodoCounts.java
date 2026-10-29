package ro.utcn.sportbuddy.lobbies.dto;

// Contoarele pentru „De făcut"; total e numărul de lângă „Agenda mea" din bara laterală.
public record TodoCounts(int pendingRequests, int attendanceToSave, int reviewsToGive, int paymentsToMake,
                         int total) {}
