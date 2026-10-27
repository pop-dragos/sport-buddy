package ro.utcn.sportbuddy.weather.service;

import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.weather.dto.DayForecastDto;
import ro.utcn.sportbuddy.weather.dto.WeatherCondition;
import ro.utcn.sportbuddy.weather.dto.WeatherResponseDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final int MAX_DAYS = 7;

    // Date fixe, repetate ciclic. Le înlocuim cu Open-Meteo la pasul următor.
    private static final WeatherCondition[] CONDITIONS = {
            WeatherCondition.RAIN, WeatherCondition.PARTLY_CLOUDY, WeatherCondition.SUNNY,
            WeatherCondition.CLOUDY, WeatherCondition.SUNNY
    };
    private static final int[] TEMP_MIN = {7, 8, 9, 6, 8};
    private static final int[] TEMP_MAX = {13, 15, 17, 12, 16};
    private static final int[] RAIN_CHANCE = {80, 30, 5, 25, 10};

    public WeatherResponseDto getForecast(Long cityId, int days) {
        int count = Math.max(1, Math.min(days, MAX_DAYS));
        LocalDate today = LocalDate.now(ZONE);

        List<DayForecastDto> forecast = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int k = i % CONDITIONS.length;
            forecast.add(new DayForecastDto(
                    today.plusDays(i), CONDITIONS[k], TEMP_MIN[k], TEMP_MAX[k], RAIN_CHANCE[k]));
        }
        return new WeatherResponseDto(cityId, forecast, buildRecommendation(forecast.get(0)));
    }

    private String buildRecommendation(DayForecastDto today) {
        if (today.condition() == WeatherCondition.RAIN || today.rainChancePercent() >= 50) {
            return "Plouă. Mai bine baschet sau volei în sală.";
        }
        if (today.tempMax() < 7) {
            return "E frig. Echipează-te bine sau alege o sală.";
        }
        return "Vreme bună pentru fotbal, tenis și padel afară.";
    }
}