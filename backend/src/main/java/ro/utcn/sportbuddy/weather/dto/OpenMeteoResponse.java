package ro.utcn.sportbuddy.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

// Partea din răspunsul Open-Meteo de care avem nevoie; listele au câte un element pe zi.
public record OpenMeteoResponse(Daily daily) {

    public record Daily(
            List<LocalDate> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_min") List<Double> tempMin,
            @JsonProperty("temperature_2m_max") List<Double> tempMax,
            @JsonProperty("precipitation_probability_max") List<Integer> rainChance
    ) {
    }
}
