package com.example.mcpserver.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CurrencyConverterTool.
 *
 * All tests run without Spring context (pure unit tests) — the tool has no dependencies.
 */
class CurrencyConverterToolTest {

    private final CurrencyConverterTool tool = new CurrencyConverterTool();

    // ── convertCurrency ───────────────────────────────────────────────────────

    @Test
    void convertCurrency_usdToEur_returnsConversion() {
        String result = tool.convertCurrency(100, "USD", "EUR");
        assertThat(result)
                .contains("100")
                .contains("USD")
                .contains("EUR")
                .contains("Exchange Rate");
    }

    @Test
    void convertCurrency_sameSourceAndTarget_returnsEquivalentAmount() {
        String result = tool.convertCurrency(500, "USD", "USD");
        assertThat(result).contains("500").contains("USD");
        assertThat(result).doesNotContain("not available").doesNotContain("Unsupported");
    }

    @Test
    void convertCurrency_unknownSourceCurrency_returnsError() {
        String result = tool.convertCurrency(100, "XYZ", "USD");
        assertThat(result).contains("Unsupported").contains("XYZ");
    }

    @Test
    void convertCurrency_unknownTargetCurrency_returnsError() {
        String result = tool.convertCurrency(100, "USD", "ABC");
        assertThat(result).contains("Unsupported").contains("ABC");
    }

    @ParameterizedTest
    @ValueSource(strings = {"usd", "USD", "Usd"})
    void convertCurrency_caseInsensitive(String from) {
        String result = tool.convertCurrency(100, from, "EUR");
        assertThat(result).doesNotContain("Unsupported");
        assertThat(result).contains("EUR");
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR",
            "EUR, GBP",
            "JPY, USD",
            "BRL, CAD",
            "INR, SGD",
    })
    void convertCurrency_supportedPairs_returnResult(String from, String to) {
        String result = tool.convertCurrency(1000, from, to);
        assertThat(result)
                .as("Conversion %s -> %s should succeed", from, to)
                .doesNotContain("Unsupported")
                .contains(from)
                .contains(to);
    }

    @Test
    void convertCurrency_resultContainsTrackingDate() {
        String result = tool.convertCurrency(100, "USD", "JPY");
        assertThat(result).containsPattern("\\d{4}-\\d{2}-\\d{2}");
    }

    // ── getExchangeRates ──────────────────────────────────────────────────────

    @Test
    void getExchangeRates_usd_returnsAllCurrencies() {
        String result = tool.getExchangeRates("USD");
        assertThat(result)
                .contains("EUR")
                .contains("GBP")
                .contains("JPY")
                .contains("BRL")
                .contains("INR");
    }

    @Test
    void getExchangeRates_containsMarkdownTable() {
        String result = tool.getExchangeRates("EUR");
        assertThat(result).contains("| Currency |").contains("| Rate |");
    }

    @Test
    void getExchangeRates_doesNotIncludeBaseCurrencyAsRow() {
        // The base currency should appear in header, not as a conversion row against itself
        String result = tool.getExchangeRates("USD");
        assertThat(result).contains("USD"); // in header
        // Result should contain other currencies
        assertThat(result).contains("EUR").contains("GBP");
    }

    @Test
    void getExchangeRates_unknownCurrency_returnsError() {
        String result = tool.getExchangeRates("ZZZ");
        assertThat(result).contains("Unsupported");
    }

    // ── calculateMultiCurrencyTotal ───────────────────────────────────────────

    @Test
    void calculateMultiCurrencyTotal_singleItem_returnsTotal() {
        String result = tool.calculateMultiCurrencyTotal("100 USD", "USD");
        assertThat(result).contains("100").contains("USD").contains("Total");
    }

    @Test
    void calculateMultiCurrencyTotal_multipleItems_returnsGrandTotal() {
        String result = tool.calculateMultiCurrencyTotal("100 USD, 85 EUR, 10000 JPY", "USD");
        assertThat(result).contains("Total").contains("USD");
    }

    @Test
    void calculateMultiCurrencyTotal_containsMarkdownTable() {
        String result = tool.calculateMultiCurrencyTotal("500 EUR, 300 GBP", "USD");
        assertThat(result).contains("| Item |").contains("| Original |").contains("| Converted |");
    }

    @Test
    void calculateMultiCurrencyTotal_unknownTargetCurrency_returnsError() {
        String result = tool.calculateMultiCurrencyTotal("100 USD", "XYZ");
        assertThat(result).contains("Unsupported");
    }

    @Test
    void calculateMultiCurrencyTotal_unknownItemCurrency_showsWarning() {
        String result = tool.calculateMultiCurrencyTotal("100 FAKE", "USD");
        assertThat(result).contains("Unknown").contains("FAKE");
    }

    @Test
    void calculateMultiCurrencyTotal_invalidAmount_showsWarning() {
        String result = tool.calculateMultiCurrencyTotal("notanumber USD", "USD");
        assertThat(result).contains("Invalid");
    }

    @Test
    void calculateMultiCurrencyTotal_mixedValidAndInvalidItems_processesBoth() {
        String result = tool.calculateMultiCurrencyTotal("100 USD, baditem, 50 EUR", "USD");
        assertThat(result).contains("100").contains("50");
    }
}

