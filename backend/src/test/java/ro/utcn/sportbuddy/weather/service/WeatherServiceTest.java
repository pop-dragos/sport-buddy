package ro.utcn.sportbuddy.weather.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.weather.dto.DayForecastDto;
import ro.utcn.sportbuddy.weather.dto.OpenMeteoResponse;
import ro.utcn.sportbuddy.weather.dto.WeatherCondition;
import ro.utcn.sportbuddy.weather.dto.WeatherResponseDto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// Catalogul și Open-Meteo sunt simulate, deci testul nu are nevoie de internet sau de baza de date.
class WeatherServiceTest {

    private static final CityDto BAIA_MARE = new CityDto(4L, "Baia Mare", 47.66, 23.58);

    private CatalogService catalogService;
    private OpenMeteoClient client;
    private MutableClock clock;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        catalogService = mock(CatalogService.class);
        client = mock(OpenMeteoClient.class);
        clock = new MutableClock(Instant.parse("2026-10-28T08:00:00Z"));
        weatherService = new WeatherService(catalogService, client, clock);
        when(catalogService.findCity(4L)).thenReturn(Optional.of(BAIA_MARE));
    }

    @Test
    void mapsOpenMeteoResponseToForecast() {
        when(client.fetchDaily(47.66, 23.58, 7)).thenReturn(response());

        WeatherResponseDto dto = weatherService.getForecast(4L, 5);

        assertEquals(4L, dto.cityId());
        assertEquals(5, dto.days().size());
        DayForecastDto first = dto.days().get(0);
        assertEquals(LocalDate.of(2026, 10, 28), first.date());
        assertEquals(WeatherCondition.SUNNY, first.condition());
        assertEquals(7, first.tempMin());   // 6.6 rotunjit
        assertEquals(15, first.tempMax());  // 14.6 rotunjit
        assertEquals(0, dto.days().get(4).rainChancePercent()); // null de la Open-Meteo
        assertEquals("Vreme bună pentru fotbal, tenis și padel afară.", dto.recommendation());
    }

    @Test
    void mapsWeatherCodes() {
        assertEquals(WeatherCondition.SUNNY, WeatherService.toCondition(0));
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherService.toCondition(2));
        assertEquals(WeatherCondition.CLOUDY, WeatherService.toCondition(45));
        assertEquals(WeatherCondition.RAIN, WeatherService.toCondition(61));
        assertEquals(WeatherCondition.RAIN, WeatherService.toCondition(95));
        assertEquals(WeatherCondition.CLOUDY, WeatherService.toCondition(null));
    }

    @Test
    void rainyTodayRecommendsIndoorSports() {
        OpenMeteoResponse rainy = response(61, 2, 3, 0, 1, 0, 0);
        when(client.fetchDaily(anyDouble(), anyDouble(), anyInt())).thenReturn(rainy);

        WeatherResponseDto dto = weatherService.getForecast(4L, 5);

        assertEquals(WeatherCondition.RAIN, dto.days().get(0).condition());
        assertEquals("Plouă. Mai bine baschet sau volei în sală.", dto.recommendation());
    }

    @Test
    void secondRequestUsesCache() {
        when(client.fetchDaily(anyDouble(), anyDouble(), anyInt())).thenReturn(response());

        weatherService.getForecast(4L, 5);
        clock.advance(Duration.ofMinutes(10));
        weatherService.getForecast(4L, 3);

        verify(client, times(1)).fetchDaily(anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void cacheExpiresAfterThirtyMinutes() {
        when(client.fetchDaily(anyDouble(), anyDouble(), anyInt())).thenReturn(response());

        weatherService.getForecast(4L, 5);
        clock.advance(Duration.ofMinutes(31));
        weatherService.getForecast(4L, 5);

        verify(client, times(2)).fetchDaily(anyDouble(), anyDouble(), anyInt());
    }

    @Test
    void oldForecastIsUsedWhenOpenMeteoFails() {
        when(client.fetchDaily(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(response())
                .thenThrow(new ResourceAccessException("timeout"));

        weatherService.getForecast(4L, 5);
        clock.advance(Duration.ofHours(2));
        WeatherResponseDto dto = weatherService.getForecast(4L, 5);

        assertEquals(5, dto.days().size());
    }

    @Test
    void unavailableWhenOpenMeteoFailsWithoutCache() {
        when(client.fetchDaily(anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new ResourceAccessException("timeout"));

        ApiException e = assertThrows(ApiException.class, () -> weatherService.getForecast(4L, 5));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatus());
        assertEquals("WEATHER_UNAVAILABLE", e.getCode());
    }

    @Test
    void unknownCityIsNotFound() {
        when(catalogService.findCity(999L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> weatherService.getForecast(999L, 5));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        verifyNoInteractions(client);
    }

    // 7 zile, cu codurile primite; temperaturi și șanse de ploaie fixe.
    private static OpenMeteoResponse response(Integer... codes) {
        LocalDate start = LocalDate.of(2026, 10, 28);
        List<LocalDate> time = List.of(start, start.plusDays(1), start.plusDays(2), start.plusDays(3),
                start.plusDays(4), start.plusDays(5), start.plusDays(6));
        return new OpenMeteoResponse(new OpenMeteoResponse.Daily(
                time,
                Arrays.asList(codes),
                List.of(6.6, 8.0, 9.0, 6.0, 8.0, 7.0, 5.0),
                List.of(14.6, 15.0, 17.0, 12.0, 16.0, 13.0, 11.0),
                Arrays.asList(10, 30, 5, 25, null, 0, 0)));
    }

    private static OpenMeteoResponse response() {
        return response(0, 2, 3, 61, 1, 0, 0);
    }

    // Ceas pe care îl putem muta înainte în teste.
    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
