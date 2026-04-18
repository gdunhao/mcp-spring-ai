package com.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * MCP Tool: Currency Converter
 *
 * Demonstrates a real-world fintech MCP tool pattern. Uses mock exchange rates
 * for demo purposes, but the pattern is identical to integrating with real
 * APIs like Open Exchange Rates, Fixer.io, or CurrencyLayer.
 *
 * Real-world use cases:
 * - International e-commerce pricing assistants
 * - Travel expense calculators
 * - Financial reporting bots
 * - Invoice currency normalization
 */
@Component
public class CurrencyConverterTool {

    private static final Map<String, Double> RATES_TO_USD = Map.ofEntries(
            Map.entry("USD", 1.0),
            Map.entry("EUR", 0.92),
            Map.entry("GBP", 0.79),
            Map.entry("JPY", 154.50),
            Map.entry("BRL", 5.12),
            Map.entry("CAD", 1.37),
            Map.entry("AUD", 1.53),
            Map.entry("CHF", 0.88),
            Map.entry("CNY", 7.24),
            Map.entry("INR", 83.45),
            Map.entry("MXN", 17.15),
            Map.entry("SGD", 1.34),
            Map.entry("AED", 3.67),
            Map.entry("KRW", 1340.0)
    );

    private static final Map<String, String> CURRENCY_NAMES = Map.ofEntries(
            Map.entry("USD", "US Dollar"),
            Map.entry("EUR", "Euro"),
            Map.entry("GBP", "British Pound"),
            Map.entry("JPY", "Japanese Yen"),
            Map.entry("BRL", "Brazilian Real"),
            Map.entry("CAD", "Canadian Dollar"),
            Map.entry("AUD", "Australian Dollar"),
            Map.entry("CHF", "Swiss Franc"),
            Map.entry("CNY", "Chinese Yuan"),
            Map.entry("INR", "Indian Rupee"),
            Map.entry("MXN", "Mexican Peso"),
            Map.entry("SGD", "Singapore Dollar"),
            Map.entry("AED", "UAE Dirham"),
            Map.entry("KRW", "South Korean Won")
    );

    @Tool(description = "Convert an amount from one currency to another. " +
            "Supported currencies: USD, EUR, GBP, JPY, BRL, CAD, AUD, CHF, CNY, INR, MXN, SGD, AED, KRW.")
    public String convertCurrency(
            @ToolParam(description = "Amount to convert") double amount,
            @ToolParam(description = "Source currency code (e.g., USD)") String fromCurrency,
            @ToolParam(description = "Target currency code (e.g., EUR)") String toCurrency) {

        String from = fromCurrency.toUpperCase().trim();
        String to = toCurrency.toUpperCase().trim();

        Double fromRate = RATES_TO_USD.get(from);
        Double toRate = RATES_TO_USD.get(to);

        if (fromRate == null) return "Unsupported currency: " + from + ". Supported: " + String.join(", ", RATES_TO_USD.keySet());
        if (toRate == null) return "Unsupported currency: " + to + ". Supported: " + String.join(", ", RATES_TO_USD.keySet());

        double amountInUsd = amount / fromRate;
        double converted = amountInUsd * toRate;
        double rate = toRate / fromRate;

        return String.format("""
                💱 Currency Conversion
                📅 Date: %s
                
                %,.2f %s (%s) → %,.2f %s (%s)
                
                Exchange Rate: 1 %s = %,.4f %s
                """,
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                amount, from, CURRENCY_NAMES.get(from),
                converted, to, CURRENCY_NAMES.get(to),
                from, rate, to);
    }

    @Tool(description = "Get current exchange rates for a currency against all supported currencies.")
    public String getExchangeRates(
            @ToolParam(description = "Base currency code (e.g., USD)") String baseCurrency) {

        String base = baseCurrency.toUpperCase().trim();
        Double baseRate = RATES_TO_USD.get(base);

        if (baseRate == null) return "Unsupported currency: " + base;

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Exchange Rates — Base: ").append(base).append(" (").append(CURRENCY_NAMES.get(base)).append(")\n");
        sb.append("📅 ").append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n\n");
        sb.append("| Currency | Rate | Name |\n");
        sb.append("|----------|------|------|\n");

        RATES_TO_USD.entrySet().stream()
                .filter(e -> !e.getKey().equals(base))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double rate = e.getValue() / baseRate;
                    sb.append(String.format("| %s | %,.4f | %s |\n", e.getKey(), rate, CURRENCY_NAMES.get(e.getKey())));
                });

        return sb.toString();
    }

    @Tool(description = "Calculate the total cost of items priced in different currencies, " +
            "converted to a single target currency. Useful for international invoicing and expense reports.")
    public String calculateMultiCurrencyTotal(
            @ToolParam(description = "Comma-separated list of amounts with currencies, e.g., '100 USD, 85 EUR, 10000 JPY'") String items,
            @ToolParam(description = "Target currency to total everything in") String targetCurrency) {

        String target = targetCurrency.toUpperCase().trim();
        Double targetRate = RATES_TO_USD.get(target);
        if (targetRate == null) return "Unsupported target currency: " + target;

        StringBuilder sb = new StringBuilder();
        sb.append("🧾 Multi-Currency Total (target: ").append(target).append(")\n\n");
        sb.append("| Item | Original | Converted |\n");
        sb.append("|------|----------|-----------|\n");

        double totalInTarget = 0;
        String[] parts = items.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            String[] tokens = trimmed.split("\\s+");
            if (tokens.length < 2) {
                sb.append("| ⚠️ Invalid: ").append(trimmed).append(" | — | — |\n");
                continue;
            }
            try {
                double amount = Double.parseDouble(tokens[0]);
                String currency = tokens[1].toUpperCase();
                Double rate = RATES_TO_USD.get(currency);
                if (rate == null) {
                    sb.append("| ⚠️ Unknown: ").append(currency).append(" | — | — |\n");
                    continue;
                }
                double converted = (amount / rate) * targetRate;
                totalInTarget += converted;
                sb.append(String.format("| %,.2f %s | %,.2f %s | %,.2f %s |\n",
                        amount, currency, amount, currency, converted, target));
            } catch (NumberFormatException e) {
                sb.append("| ⚠️ Invalid amount: ").append(trimmed).append(" | — | — |\n");
            }
        }

        sb.append(String.format("\n**Total: %,.2f %s**\n", totalInTarget, target));
        return sb.toString();
    }
}

