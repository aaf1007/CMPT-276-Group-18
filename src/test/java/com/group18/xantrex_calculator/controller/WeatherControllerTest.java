package com.group18.xantrex_calculator.controller;

import com.group18.xantrex_calculator.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void testGetMinTemp_Success() throws Exception {
        // Arrange
        String city = "Vancouver";
        String country = "CA";
        double expectedTemp = 5.0;
        when(weatherService.getMinTemperature(city, country, null)).thenReturn(expectedTemp);

        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("city", city)
                .param("country", country))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedTemp)));
    }

    @Test
    void testGetMinTemp_CityNotFound() throws Exception {
        // Arrange
        String city = "InvalidCity";
        String country = "XX";
        when(weatherService.getMinTemperature(city, country, null))
                .thenThrow(new WeatherService.CityNotFoundException(city, country));

        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("city", city)
                .param("country", country))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Location not found: \"InvalidCity, XX\". Please check your city name and 2-letter country code (e.g. CA, US, GB)."));
    }

    @Test
    void testGetMinTemp_OtherException() throws Exception {
        // Arrange
        String city = "Vancouver";
        String country = "CA";
        when(weatherService.getMinTemperature(city, country, null))
                .thenThrow(new RuntimeException("API error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("city", city)
                .param("country", country))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unable to retrieve weather data. Please try again."));
    }

    @Test
    void testGetMinTemp_WithRegion() throws Exception {
        // Arrange
        String city = "Vancouver";
        String country = "CA";
        String region = "BC";
        double expectedTemp = -3.0;
        when(weatherService.getMinTemperature(city, country, region)).thenReturn(expectedTemp);

        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("city", city)
                .param("country", country)
                .param("region", region))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedTemp)));

        verify(weatherService).getMinTemperature(city, country, region);
    }

    @Test
    void testSearchCities() throws Exception {
        when(weatherService.searchCities("Portl", "US", "Oregon"))
                .thenReturn(java.util.List.of(
                        new WeatherService.CitySuggestion("Portland", "Oregon", "United States")));

        mockMvc.perform(get("/api/weather/cities")
                .param("city", "Portl")
                .param("country", "US")
                .param("region", "Oregon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Portland"))
                .andExpect(jsonPath("$[0].admin1").value("Oregon"))
                .andExpect(jsonPath("$[0].country").value("United States"));
    }

    @Test
    void testGetMinTemp_MissingCityParam() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("country", "CA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMinTemp_MissingCountryParam() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/min-temp")
                .param("city", "Vancouver"))
                .andExpect(status().isBadRequest());
    }
}
