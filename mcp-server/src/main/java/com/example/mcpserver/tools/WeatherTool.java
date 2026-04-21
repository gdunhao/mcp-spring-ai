package com.example.mcpserver.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

/**
 * MCP Tool: Weather Information Service
 *
 * Demonstrates how MCP tools can wrap external API calls and present them
 * to an LLM. This implementation uses mock data for demo purposes, but
 * the pattern is identical to wrapping real weather APIs (OpenWeatherMap,
 * WeatherAPI, etc.).
 *
 * Real-world use cases:
 * - AI travel assistants with live weather integration
 * - Agricultural planning chatbots
 * - Event planning assistants
 * - Smart home automation (natural language weather queries)
 */
@Component
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    // Mock weather data for demo cities
    private static final Map<String, MockWeatherData> CITY_WEATHER = Map.ofEntries(
            Map.entry("new york", new MockWeatherData("New York", "US", 22, 18, 75, 12, "Partly Cloudy", "🌤️")),
            Map.entry("london", new MockWeatherData("London", "UK", 15, 11, 82, 18, "Overcast", "☁️")),
            Map.entry("tokyo", new MockWeatherData("Tokyo", "JP", 28, 24, 65, 8, "Sunny", "☀️")),
            Map.entry("paris", new MockWeatherData("Paris", "FR", 20, 16, 70, 14, "Light Rain", "🌧️")),
            Map.entry("sydney", new MockWeatherData("Sydney", "AU", 19, 14, 60, 22, "Windy", "💨")),
            Map.entry("são paulo", new MockWeatherData("São Paulo", "BR", 26, 21, 78, 6, "Thunderstorms", "⛈️")),
            Map.entry("sao paulo", new MockWeatherData("São Paulo", "BR", 26, 21, 78, 6, "Thunderstorms", "⛈️")),
            Map.entry("berlin", new MockWeatherData("Berlin", "DE", 17, 12, 68, 16, "Cloudy", "☁️")),
            Map.entry("mumbai", new MockWeatherData("Mumbai", "IN", 33, 28, 85, 10, "Humid", "🌡️")),
            Map.entry("dubai", new MockWeatherData("Dubai", "AE", 38, 30, 45, 5, "Clear Sky", "☀️")),
            Map.entry("toronto", new MockWeatherData("Toronto", "CA", 20, 15, 55, 20, "Clear", "☀️")),
            Map.entry("mexico city", new MockWeatherData("Mexico City", "MX", 23, 14, 50, 8, "Partly Cloudy", "🌤️")),
            Map.entry("singapore", new MockWeatherData("Singapore", "SG", 31, 27, 88, 7, "Tropical Rain", "🌴🌧️"))
    );

    @Tool(description = "Get current weather information for a city. " +
            "Returns temperature, humidity, wind speed, and conditions. " +
            "Supported cities include: New York, London, Tokyo, Paris, Sydney, " +
            "São Paulo, Berlin, Mumbai, Dubai, Toronto, Mexico City, Singapore.")
    public String getCurrentWeather(
            @ToolParam(description = "Name of the city to get weather for") String city) {
        log.debug("getCurrentWeather() — city: '{}'", city);
        MockWeatherData data = CITY_WEATHER.get(city.toLowerCase().trim());

        if (data == null) {
            log.warn("getCurrentWeather() — weather data not found for city: '{}'", city);
            return "Weather data not available for '" + city + "'. " +
                    "Available cities: " + String.join(", ",
                    CITY_WEATHER.values().stream().map(d -> d.city).distinct().sorted().toList());
        }

        log.info("getCurrentWeather() — returning weather for {}, {}: {}°C, {}",
                data.city, data.country, data.tempC, data.conditions);
        // Add some random variation to make it feel dynamic
        Random rand = new Random(LocalDate.now().toEpochDay());
        int tempVariation = rand.nextInt(5) - 2;

        return String.format("""
                %s Weather Report for %s, %s
                📅 Date: %s
                🌡️ Temperature: %d°C (feels like %d°C)
                💧 Humidity: %d%%
                💨 Wind Speed: %d km/h
                🌤️ Conditions: %s
                """,
                data.emoji, data.city, data.country,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                data.tempC + tempVariation, data.feelsLikeC + tempVariation,
                data.humidity, data.windKmh, data.conditions);
    }

    @Tool(description = "Get a weather forecast for the next 3 days for a city. " +
            "Returns daily high/low temperatures and expected conditions.")
    public String getWeatherForecast(
            @ToolParam(description = "Name of the city to get forecast for") String city) {
        log.debug("getWeatherForecast() — city: '{}'", city);
        MockWeatherData data = CITY_WEATHER.get(city.toLowerCase().trim());

        if (data == null) {
            log.warn("getWeatherForecast() — forecast data not found for city: '{}'", city);
            return "Forecast data not available for '" + city + "'.";
        }

        log.info("getWeatherForecast() — returning 3-day forecast for {}, {}", data.city, data.country);

        StringBuilder sb = new StringBuilder();
        sb.append("📅 3-Day Forecast for ").append(data.city).append(", ").append(data.country).append("\n\n");

        String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Clear"};
        Random rand = new Random(city.hashCode());

        for (int i = 1; i <= 3; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            int highVar = rand.nextInt(6) - 2;
            int lowVar = rand.nextInt(4) - 1;
            String cond = conditions[rand.nextInt(conditions.length)];

            sb.append(String.format("  %s: High %d°C / Low %d°C — %s\n",
                    date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                    data.tempC + highVar, data.feelsLikeC + lowVar, cond));
        }
        return sb.toString();
    }

    @Tool(description = "Compare weather between two cities. Useful for travel planning.")
    public String compareWeather(
            @ToolParam(description = "First city name") String city1,
            @ToolParam(description = "Second city name") String city2) {
        log.debug("compareWeather() — comparing '{}' vs '{}'", city1, city2);
        MockWeatherData data1 = CITY_WEATHER.get(city1.toLowerCase().trim());
        MockWeatherData data2 = CITY_WEATHER.get(city2.toLowerCase().trim());

        if (data1 == null) {
            log.warn("compareWeather() — data not found for city: '{}'", city1);
            return "Weather data not available for '" + city1 + "'.";
        }
        if (data2 == null) {
            log.warn("compareWeather() — data not found for city: '{}'", city2);
            return "Weather data not available for '" + city2 + "'.";
        }

        log.info("compareWeather() — {}: {}°C vs {}: {}°C (diff: {}°C)",
                data1.city, data1.tempC, data2.city, data2.tempC, Math.abs(data1.tempC - data2.tempC));

        return String.format("""
                Weather Comparison: %s vs %s
                
                | Metric       | %s | %s |
                |-------------|------|------|
                | Temperature | %d°C | %d°C |
                | Humidity    | %d%%  | %d%%  |
                | Wind Speed  | %d km/h | %d km/h |
                | Conditions  | %s | %s |
                
                Temperature difference: %d°C
                """,
                data1.city, data2.city,
                data1.city, data2.city,
                data1.tempC, data2.tempC,
                data1.humidity, data2.humidity,
                data1.windKmh, data2.windKmh,
                data1.conditions, data2.conditions,
                Math.abs(data1.tempC - data2.tempC));
    }

    private record MockWeatherData(
            String city, String country, int tempC, int feelsLikeC,
            int humidity, int windKmh, String conditions, String emoji) {
    }
}

