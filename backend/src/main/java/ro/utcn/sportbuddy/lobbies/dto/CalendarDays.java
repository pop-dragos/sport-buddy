package ro.utcn.sportbuddy.lobbies.dto;

import java.time.LocalDate;
import java.util.List;

// Zilele dintr-o lună care au cel puțin un lobby (punctul de pe calendar).
public record CalendarDays(List<LocalDate> days) {}
