package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WeatherTool.
 */
class WeatherToolTest {

    private final WeatherTool tool = new WeatherTool();

    // ── getCurrentWeather ─────────────────────────────────────────────────────

    @Test
    void getCurrentWeather_knownCity_returnsData() {
        String result = tool.getCurrentWeather("Tokyo");
        assertThat(result).contains("Tokyo").contains("Temperature");
    }

    @Test
    void getCurrentWeather_unknownCity_returnsError() {
        String result = tool.getCurrentWeather("Atlantis");
        assertThat(result).contains("not available");
    }

    @Test
    void getCurrentWeather_unknownCity_listsSupportedCities() {
        String result = tool.getCurrentWeather("Atlantis");
        assertThat(result).containsIgnoringCase("Tokyo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"tokyo", "TOKYO", "Tokyo", "  Tokyo  "})
    void getCurrentWeather_caseAndSpaceInsensitive(String input) {
        String result = tool.getCurrentWeather(input);
        assertThat(result).contains("Tokyo");
    }

    @Test
    void getCurrentWeather_returnsDateTemperatureHumidityWind() {
        String result = tool.getCurrentWeather("London");
        assertThat(result)
                .contains("London")
                .contains("Temperature")
                .contains("Humidity")
                .contains("Wind Speed")
                .contains("Conditions");
    }

    @Test
    void getCurrentWeather_allSupportedCities_returnData() {
        for (String city : new String[]{"New York", "London", "Tokyo", "Paris", "Sydney",
                "Berlin", "Mumbai", "Dubai", "Toronto", "Mexico City", "Singapore"}) {
            String result = tool.getCurrentWeather(city);
            assertThat(result).as("Weather for %s should not contain 'not available'", city)
                    .doesNotContain("not available");
        }
    }

    // ── getWeatherForecast ────────────────────────────────────────────────────

    @Test
    void getWeatherForecast_returnsThreeDays() {
        String result = tool.getWeatherForecast("London");
        assertThat(result).contains("3-Day Forecast").contains("London");
    }

    @Test
    void getWeatherForecast_unknownCity_returnsError() {
        String result = tool.getWeatherForecast("Narnia");
        assertThat(result).contains("not available");
    }

    @Test
    void getWeatherForecast_containsHighLow() {
        String result = tool.getWeatherForecast("Tokyo");
        assertThat(result).contains("High").contains("Low");
    }

    // ── compareWeather ────────────────────────────────────────────────────────

    @Test
    void compareWeather_twoValidCities_returnsComparison() {
        String result = tool.compareWeather("Tokyo", "Paris");
        assertThat(result).contains("Comparison").contains("Tokyo").contains("Paris");
    }

    @Test
    void compareWeather_containsMetrics() {
        String result = tool.compareWeather("London", "Dubai");
        assertThat(result)
                .contains("Temperature")
                .contains("Humidity")
                .contains("Wind Speed")
                .contains("Conditions");
    }

    @Test
    void compareWeather_firstCityUnknown_returnsError() {
        String result = tool.compareWeather("Narnia", "Tokyo");
        assertThat(result).contains("not available");
    }

    @Test
    void compareWeather_secondCityUnknown_returnsError() {
        String result = tool.compareWeather("Tokyo", "Narnia");
        assertThat(result).contains("not available");
    }

    @Test
    void compareWeather_showsTemperatureDifference() {
        String result = tool.compareWeather("Tokyo", "London");
        assertThat(result).contains("Temperature difference");
    }
}

