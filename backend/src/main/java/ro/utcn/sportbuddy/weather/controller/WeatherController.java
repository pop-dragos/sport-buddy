package ro.utcn.sportbuddy.weather.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.weather.dto.WeatherResponseDto;
import ro.utcn.sportbuddy.weather.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponseDto getWeather(@RequestParam Long cityId,
                                         @RequestParam(defaultValue = "5") int days) {
        return weatherService.getForecast(cityId, days);
    }
}