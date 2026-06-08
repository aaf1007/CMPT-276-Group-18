package com.group18.xantrex_calculator.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    private static final Set<String> US_CA_COUNTRY_TOKENS = Set.of(
            "US", "USA", "U.S.", "U.S.A.", "UNITED STATES", "UNITED STATES OF AMERICA", "AMERICA",
            "CA", "CAN", "CANADA");

    private static final Map<String, String> REGION_ABBREVIATIONS = Map.ofEntries(
            Map.entry("AB", "Alberta"), Map.entry("BC", "British Columbia"), Map.entry("MB", "Manitoba"),
            Map.entry("NB", "New Brunswick"), Map.entry("NL", "Newfoundland and Labrador"),
            Map.entry("NS", "Nova Scotia"), Map.entry("NT", "Northwest Territories"), Map.entry("NU", "Nunavut"),
            Map.entry("ON", "Ontario"), Map.entry("PE", "Prince Edward Island"), Map.entry("QC", "Quebec"),
            Map.entry("SK", "Saskatchewan"), Map.entry("YT", "Yukon"),
            Map.entry("AL", "Alabama"), Map.entry("AK", "Alaska"), Map.entry("AZ", "Arizona"),
            Map.entry("AR", "Arkansas"), Map.entry("CA", "California"), Map.entry("CO", "Colorado"),
            Map.entry("CT", "Connecticut"), Map.entry("DE", "Delaware"), Map.entry("FL", "Florida"),
            Map.entry("GA", "Georgia"), Map.entry("HI", "Hawaii"), Map.entry("ID", "Idaho"),
            Map.entry("IL", "Illinois"), Map.entry("IN", "Indiana"), Map.entry("IA", "Iowa"),
            Map.entry("KS", "Kansas"), Map.entry("KY", "Kentucky"), Map.entry("LA", "Louisiana"),
            Map.entry("ME", "Maine"), Map.entry("MD", "Maryland"), Map.entry("MA", "Massachusetts"),
            Map.entry("MI", "Michigan"), Map.entry("MN", "Minnesota"), Map.entry("MS", "Mississippi"),
            Map.entry("MO", "Missouri"), Map.entry("MT", "Montana"), Map.entry("NE", "Nebraska"),
            Map.entry("NV", "Nevada"), Map.entry("NH", "New Hampshire"), Map.entry("NJ", "New Jersey"),
            Map.entry("NM", "New Mexico"), Map.entry("NY", "New York"), Map.entry("NC", "North Carolina"),
            Map.entry("ND", "North Dakota"), Map.entry("OH", "Ohio"), Map.entry("OK", "Oklahoma"),
            Map.entry("OR", "Oregon"), Map.entry("PA", "Pennsylvania"), Map.entry("RI", "Rhode Island"),
            Map.entry("SC", "South Carolina"), Map.entry("SD", "South Dakota"), Map.entry("TN", "Tennessee"),
            Map.entry("TX", "Texas"), Map.entry("UT", "Utah"), Map.entry("VT", "Vermont"),
            Map.entry("VA", "Virginia"), Map.entry("WA", "Washington"), Map.entry("WV", "West Virginia"),
            Map.entry("WI", "Wisconsin"), Map.entry("WY", "Wyoming"), Map.entry("DC", "District of Columbia"));

    public static boolean requiresRegion(String country) {
        return country != null && US_CA_COUNTRY_TOKENS.contains(country.trim().toUpperCase());
    }

    private boolean countryMatches(String input, Map location) {
        String code = (String) location.get("country_code");
        String name = (String) location.get("country");
        String c = input.trim();
        return (code != null && code.equalsIgnoreCase(c)) || (name != null && name.equalsIgnoreCase(c));
    }

    private boolean regionMatches(String input, Map location) {
        String admin1 = (String) location.get("admin1");
        if (admin1 == null) {
            return false;
        }
        String r = input.trim();
        if (admin1.equalsIgnoreCase(r)) {
            return true;
        }
        String expanded = REGION_ABBREVIATIONS.get(r.toUpperCase());
        if (expanded != null) {
            return admin1.equalsIgnoreCase(expanded);
        }
        return admin1.toLowerCase().contains(r.toLowerCase());
    }

    private double[] getCoordinates(String city, String country, String region) {
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + city + "&count=10&language=en&format=json";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("results")) {
                throw new CityNotFoundException(city, country);
            }

            List results = (List) response.get("results");
            if (results.isEmpty()) {
                throw new CityNotFoundException(city, country);
            }

            boolean hasCountry = country != null && !country.isBlank();
            boolean hasRegion = region != null && !region.isBlank();

            for (Object item : results) {
                Map location = (Map) item;
                if (hasCountry && !countryMatches(country, location)) {
                    continue;
                }
                if (hasRegion && !regionMatches(region, location)) {
                    continue;
                }
                double lat = ((Number) location.get("latitude")).doubleValue();
                double lon = ((Number) location.get("longitude")).doubleValue();
                return new double[]{lat, lon};
            }

            throw new CityNotFoundException(city, country);

        } catch (CityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed for: " + city + " - " + e.getMessage());
        }
    }

    public double getMinTemperature(String city, String country) {
        return getMinTemperature(city, country, null);
    }

    public double getMinTemperature(String city, String country, String region) {
        double[] coords = getCoordinates(city, country, region);
        double lat = coords[0];
        double lon = coords[1];

        try {
            LocalDate endDate   = LocalDate.now().minusDays(2);
            LocalDate startDate = endDate.minusYears(1);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String url = "https://archive-api.open-meteo.com/v1/archive"
                    + "?latitude="   + lat
                    + "&longitude="  + lon
                    + "&start_date=" + startDate.format(fmt)
                    + "&end_date="   + endDate.format(fmt)
                    + "&daily=temperature_2m_min"
                    + "&temperature_unit=celsius"
                    + "&timezone=auto";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("daily")) {
                throw new RuntimeException("Historical weather data not found for: " + city);
            }

            Map daily = (Map) response.get("daily");
            List tempMinList = (List) daily.get("temperature_2m_min");

            if (tempMinList == null || tempMinList.isEmpty()) {
                throw new RuntimeException("No historical temperature data available");
            }

            List<Double> temps = (List<Double>) tempMinList.stream()
                    .filter(t -> t != null)
                    .map(t -> (t instanceof Number)
                            ? ((Number) t).doubleValue()
                            : Double.parseDouble(t.toString()))
                    .collect(Collectors.toList());

            if (temps.isEmpty()) {
                return 0;
            }

            return temps.stream().mapToDouble(Double::doubleValue).min().orElse(0);

        } catch (Exception e) {
            System.err.println("WeatherService error for " + city + ": " + e.getMessage());
            return 0;
        }
    }

    public List<CitySuggestion> searchCities(String city, String country, String region) {
        if (city == null || city.isBlank()) {
            return List.of();
        }
        try {
            String url = "https://geocoding-api.open-meteo.com/v1/search?name="
                    + city + "&count=100&language=en&format=json";

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                return List.of();
            }

            List results = (List) response.get("results");
            boolean hasCountry = country != null && !country.isBlank();
            boolean hasRegion = region != null && !region.isBlank();

            List<CitySuggestion> suggestions = new ArrayList<>();
            for (Object item : results) {
                Map location = (Map) item;
                if (hasCountry && !countryMatches(country, location)) {
                    continue;
                }
                if (hasRegion && !regionMatches(region, location)) {
                    continue;
                }
                suggestions.add(new CitySuggestion(
                        (String) location.get("name"),
                        (String) location.get("admin1"),
                        (String) location.get("country")));
                if (suggestions.size() >= 10) {
                    break;
                }
            }
            return suggestions;

        } catch (Exception e) {
            System.err.println("WeatherService city search error for " + city + ": " + e.getMessage());
            return List.of();
        }
    }

    public record CitySuggestion(String name, String admin1, String country) {
    }

    // Custom exception
    public static class CityNotFoundException extends RuntimeException {
        private final String city;
        private final String country;

        public CityNotFoundException(String city, String country) {
            super("City not found: " + city + (country != null && !country.isBlank() ? ", " + country.toUpperCase() : ""));
            this.city    = city;
            this.country = country;
        }

        public String getCity()    { return city; }
        public String getCountry() { return country; }
    }
}