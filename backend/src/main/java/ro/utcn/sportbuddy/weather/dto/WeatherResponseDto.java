package ro.utcn.sportbuddy.weather.dto;

import java.util.List;

public record WeatherResponseDto(
        Long cityId,
        List<DayForecastDto> days,
        String recommendation
) {
}