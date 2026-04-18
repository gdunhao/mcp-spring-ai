package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherTool.
 */
class WeatherToolTest {

    private final WeatherTool tool = new WeatherTool();

    @Test
    void getCurrentWeather_knownCity_returnsData() {
        String result = tool.getCurrentWeather("Tokyo");
        assertTrue(result.contains("Tokyo"));
        assertTrue(result.contains("Temperature"));
    }

    @Test
    void getCurrentWeather_unknownCity_returnsError() {
        String result = tool.getCurrentWeather("Atlantis");
        assertTrue(result.contains("not available"));
    }

    @Test
    void getWeatherForecast_returnsThreeDays() {
        String result = tool.getWeatherForecast("London");
        assertTrue(result.contains("3-Day Forecast"));
        assertTrue(result.contains("London"));
    }

    @Test
    void compareWeather_twoValidCities() {
        String result = tool.compareWeather("Tokyo", "Paris");
        assertTrue(result.contains("Comparison"));
        assertTrue(result.contains("Tokyo"));
        assertTrue(result.contains("Paris"));
    }

    @Test
    void getCurrentWeather_caseInsensitive() {
        String result = tool.getCurrentWeather("TOKYO");
        assertTrue(result.contains("Tokyo"));
    }
}

