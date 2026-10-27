package ro.utcn.sportbuddy.weather.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ro.utcn.sportbuddy.weather.dto.OpenMeteoResponse;

import java.time.Duration;

// Doar apelul HTTP către Open-Meteo (gratuit, fără cheie). Regulile stau în WeatherService.
@Component
public class OpenMeteoClient {

    private static final String BASE_URL = "https://api.open-meteo.com/v1";
    private static final Duration TIMEOUT = Duration.ofSeconds(4);

    private final RestClient restClient;

    public OpenMeteoClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    public OpenMeteoResponse fetchDaily(double latitude, double longitude, int days) {
        return restClient.get()
                .uri(uri -> uri.path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("daily", "weather_code,temperature_2m_min,temperature_2m_max,precipitation_probability_max")
                        .queryParam("timezone", "Europe/Bucharest")
                        .queryParam("forecast_days", days)
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);
    }
}
