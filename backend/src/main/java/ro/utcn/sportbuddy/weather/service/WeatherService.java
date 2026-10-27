package ro.utcn.sportbuddy.weather.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private static final int MAX_DAYS = 7;
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final CatalogService catalogService;
    private final OpenMeteoClient openMeteoClient;
    private final Clock clock;

    // Prognoza pe MAX_DAYS zile, ținută în memorie pe oraș (vremea nu are tabelă).
    private final Map<Long, CachedForecast> cache = new ConcurrentHashMap<>();

    private record CachedForecast(List<DayForecastDto> days, Instant fetchedAt) {
    }

    @Autowired
    public WeatherService(CatalogService catalogService, OpenMeteoClient openMeteoClient) {
        this(catalogService, openMeteoClient, Clock.systemUTC());
    }

    // Folosit în teste, ca să putem „da ceasul înainte" fără să așteptăm 30 de minute.
    WeatherService(CatalogService catalogService, OpenMeteoClient openMeteoClient, Clock clock) {
        this.catalogService = catalogService;
        this.openMeteoClient = openMeteoClient;
        this.clock = clock;
    }

    public WeatherResponseDto getForecast(Long cityId, int days) {
        int count = Math.max(1, Math.min(days, MAX_DAYS));
        CityDto city = catalogService.findCity(cityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Orașul nu există."));

        List<DayForecastDto> forecast = loadForecast(city);
        List<DayForecastDto> selected = forecast.subList(0, Math.min(count, forecast.size()));
        return new WeatherResponseDto(cityId, selected, buildRecommendation(selected.get(0)));
    }

    private List<DayForecastDto> loadForecast(CityDto city) {
        CachedForecast cached = cache.get(city.id());
        Instant now = clock.instant();
        if (cached != null && cached.fetchedAt().plus(CACHE_TTL).isAfter(now)) {
            return cached.days();
        }

        try {
            OpenMeteoResponse response = openMeteoClient.fetchDaily(city.latitude(), city.longitude(), MAX_DAYS);
            List<DayForecastDto> days = toDays(response);
            cache.put(city.id(), new CachedForecast(days, now));
            return days;
        } catch (RuntimeException e) {
            // Fără internet sau Open-Meteo picat: mai bine o prognoză mai veche decât nimic.
            log.warn("Open-Meteo indisponibil pentru {}: {}", city.name(), e.getMessage());
            if (cached != null) {
                return cached.days();
            }
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER_UNAVAILABLE",
                    "Prognoza nu este disponibilă acum.");
        }
    }

    private List<DayForecastDto> toDays(OpenMeteoResponse response) {
        if (response == null || response.daily() == null || response.daily().time() == null
                || response.daily().time().isEmpty()) {
            throw new IllegalStateException("Răspuns gol de la Open-Meteo");
        }
        OpenMeteoResponse.Daily d = response.daily();
        List<DayForecastDto> days = new ArrayList<>();
        for (int i = 0; i < d.time().size(); i++) {
            Integer rain = d.rainChance().get(i);
            days.add(new DayForecastDto(
                    d.time().get(i),
                    toCondition(d.weatherCode().get(i)),
                    (int) Math.round(d.tempMin().get(i)),
                    (int) Math.round(d.tempMax().get(i)),
                    rain == null ? 0 : rain));
        }
        return days;
    }

    // Codurile WMO folosite de Open-Meteo, reduse la cele patru stări din interfață.
    static WeatherCondition toCondition(Integer code) {
        if (code == null) return WeatherCondition.CLOUDY;
        if (code == 0) return WeatherCondition.SUNNY;
        if (code == 1 || code == 2) return WeatherCondition.PARTLY_CLOUDY;
        if (code == 3 || code == 45 || code == 48) return WeatherCondition.CLOUDY;
        return WeatherCondition.RAIN; // burniță, ploaie, ninsoare, averse, furtună
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
