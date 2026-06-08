package com.group18.xantrex_calculator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.group18.xantrex_calculator.service.WeatherService;
import com.group18.xantrex_calculator.service.WeatherService.CityNotFoundException;
import com.group18.xantrex_calculator.service.WeatherService.CitySuggestion;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/cities")
    public List<CitySuggestion> searchCities(@RequestParam String city,
                                             @RequestParam(required = false) String country,
                                             @RequestParam(required = false) String region) {
        return weatherService.searchCities(city, country, region);
    }

    @GetMapping("/min-temp")
    public ResponseEntity<?> getMinTemp(@RequestParam String city,
                                        @RequestParam String country,
                                        @RequestParam(required = false) String region) {
        try {
            double temp = weatherService.getMinTemperature(city, country, region);
            return ResponseEntity.ok(temp);

        } catch (CityNotFoundException e) {
            // Return 400 with a user-friendly message the frontend can display
            return ResponseEntity
                    .badRequest()
                    .body("Location not found: \"" + e.getCity() + ", " + e.getCountry().toUpperCase()
                            + "\". Please check your city name and 2-letter country code (e.g. CA, US, GB).");

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body("Unable to retrieve weather data. Please try again.");
        }
    }
}