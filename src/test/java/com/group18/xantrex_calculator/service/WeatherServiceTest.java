package com.group18.xantrex_calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Spy
    private WeatherService weatherService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // Inject mock RestTemplate into the private field
        java.lang.reflect.Field field = WeatherService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(weatherService, restTemplate);
    }

    @Test
    void testGetMinTemperature_Success() {
        // Mock geocoding response
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResponse.put("results", Arrays.asList(geoResult));

        // Mock weather response
        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("temperature_2m_min", Arrays.asList(-5.0, 0.0, 5.0));
        weatherResponse.put("daily", daily);

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "CA");

        assertEquals(-5.0, result, 0.001);
    }

    @Test
    void testGetMinTemperature_CityNotFound_NoResults() {
        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", Arrays.asList());

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(geoResponse);

        assertThrows(WeatherService.CityNotFoundException.class, () -> {
            weatherService.getMinTemperature("InvalidCity", "XX");
        });
    }

    @Test
    void testGetMinTemperature_CityNotFound_CountryMismatch() {
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "US"); // Wrong country
        geoResponse.put("results", Arrays.asList(geoResult));

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);

        assertThrows(WeatherService.CityNotFoundException.class, () -> {
            weatherService.getMinTemperature("Vancouver", "CA");
        });
    }

    @Test
    void testGetMinTemperature_NoCountryFilter() {
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResponse.put("results", Arrays.asList(geoResult));

        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("temperature_2m_min", Arrays.asList(10.0, 15.0));
        weatherResponse.put("daily", daily);

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "");

        assertEquals(10.0, result, 0.001);
    }

    @Test
    void testGetMinTemperature_WeatherApiFailure() {
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResponse.put("results", Arrays.asList(geoResult));

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenThrow(new RuntimeException("API error"));

        double result = weatherService.getMinTemperature("Vancouver", "CA");

        assertEquals(0.0, result, 0.001); // Returns 0 on error
    }

    @Test
    void testGetMinTemperature_NoWeatherData() {
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResponse.put("results", Arrays.asList(geoResult));

        Map<String, Object> weatherResponse = new HashMap<>();
        weatherResponse.put("daily", new HashMap<>()); // No temperature_2m_min

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "CA");

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testGetMinTemperature_EmptyTempList() {
        Map<String, Object> geoResponse = new HashMap<>();
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResponse.put("results", Arrays.asList(geoResult));

        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("temperature_2m_min", Arrays.asList()); // Empty list
        weatherResponse.put("daily", daily);

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "CA");

        assertEquals(0.0, result, 0.001);
    }

    @Test
    void testGetMinTemperature_CountryByFullName() {
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResult.put("country", "Canada");
        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", Arrays.asList(geoResult));

        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("temperature_2m_min", Arrays.asList(-3.0, 1.0));
        weatherResponse.put("daily", daily);

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "Canada", null);

        assertEquals(-3.0, result, 0.001);
    }

    @Test
    void testGetMinTemperature_RegionDisambiguatesCity() {
        Map<String, Object> washington = new HashMap<>();
        washington.put("latitude", 47.6062);
        washington.put("longitude", -122.3321);
        washington.put("country_code", "US");
        washington.put("country", "United States");
        washington.put("admin1", "Washington");

        Map<String, Object> britishColumbia = new HashMap<>();
        britishColumbia.put("latitude", 49.2827);
        britishColumbia.put("longitude", -123.1207);
        britishColumbia.put("country_code", "CA");
        britishColumbia.put("country", "Canada");
        britishColumbia.put("admin1", "British Columbia");

        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", Arrays.asList(washington, britishColumbia));

        Map<String, Object> weatherResponse = new HashMap<>();
        Map<String, Object> daily = new HashMap<>();
        daily.put("temperature_2m_min", Arrays.asList(-8.0, -2.0));
        weatherResponse.put("daily", daily);

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);
        when(restTemplate.getForObject(contains("archive-api"), eq(Map.class))).thenReturn(weatherResponse);

        double result = weatherService.getMinTemperature("Vancouver", "CA", "BC");

        assertEquals(-8.0, result, 0.001);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate, atLeastOnce()).getForObject(urlCaptor.capture(), eq(Map.class));
        boolean usedBritishColumbiaCoords = urlCaptor.getAllValues().stream()
                .anyMatch(url -> url.contains("archive-api") && url.contains("latitude=49.2827"));
        assertTrue(usedBritishColumbiaCoords);
    }

    @Test
    void testGetMinTemperature_RegionMismatchThrows() {
        Map<String, Object> geoResult = new HashMap<>();
        geoResult.put("latitude", 49.2827);
        geoResult.put("longitude", -123.1207);
        geoResult.put("country_code", "CA");
        geoResult.put("country", "Canada");
        geoResult.put("admin1", "British Columbia");
        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", Arrays.asList(geoResult));

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);

        assertThrows(WeatherService.CityNotFoundException.class, () -> {
            weatherService.getMinTemperature("Vancouver", "CA", "Ontario");
        });
    }

    @Test
    void testRequiresRegion() {
        assertTrue(WeatherService.requiresRegion("US"));
        assertTrue(WeatherService.requiresRegion("usa"));
        assertTrue(WeatherService.requiresRegion("United States"));
        assertTrue(WeatherService.requiresRegion("Canada"));
        assertTrue(WeatherService.requiresRegion("ca"));
        assertFalse(WeatherService.requiresRegion("GB"));
        assertFalse(WeatherService.requiresRegion("Mexico"));
        assertFalse(WeatherService.requiresRegion(""));
        assertFalse(WeatherService.requiresRegion(null));
    }

    @Test
    void testSearchCities_FiltersByCountryAndRegion() {
        Map<String, Object> portlandOregon = new HashMap<>();
        portlandOregon.put("name", "Portland");
        portlandOregon.put("country_code", "US");
        portlandOregon.put("country", "United States");
        portlandOregon.put("admin1", "Oregon");

        Map<String, Object> portlandMaine = new HashMap<>();
        portlandMaine.put("name", "Portland");
        portlandMaine.put("country_code", "US");
        portlandMaine.put("country", "United States");
        portlandMaine.put("admin1", "Maine");

        Map<String, Object> geoResponse = new HashMap<>();
        geoResponse.put("results", Arrays.asList(portlandOregon, portlandMaine));

        when(restTemplate.getForObject(contains("geocoding-api"), eq(Map.class))).thenReturn(geoResponse);

        List<WeatherService.CitySuggestion> result = weatherService.searchCities("Portland", "US", "Oregon");

        assertEquals(1, result.size());
        assertEquals("Portland", result.get(0).name());
        assertEquals("Oregon", result.get(0).admin1());
        assertEquals("United States", result.get(0).country());
    }

    @Test
    void testSearchCities_BlankCityReturnsEmpty() {
        List<WeatherService.CitySuggestion> result = weatherService.searchCities("", "US", "Oregon");
        assertTrue(result.isEmpty());
    }
}
