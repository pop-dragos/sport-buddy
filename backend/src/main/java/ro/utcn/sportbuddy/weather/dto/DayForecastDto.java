package ro.utcn.sportbuddy.weather.dto;

import java.time.LocalDate;

public record DayForecastDto(
        LocalDate date,
        WeatherCondition condition,
        int tempMin,
        int tempMax,
        int rainChancePercent
) {
}