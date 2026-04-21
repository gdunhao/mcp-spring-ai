package com.example.mcpserver.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MCP Tool: Notification Service
 *
 * Demonstrates a real-world enterprise messaging tool pattern. Simulates sending
 * notifications via multiple channels (email, Slack, SMS) and tracking delivery.
 *
 * In production, you'd replace the mock implementations with:
 * - JavaMail / SendGrid / SES for email
 * - Slack Web API for Slack messages
 * - Twilio / SNS for SMS
 *
 * Real-world use cases:
 * - AI-powered incident response bots
 * - Automated report distribution
 * - Customer notification orchestration
 * - DevOps alert management
 */
@Component
public class NotificationTool {

    private static final Logger log = LoggerFactory.getLogger(NotificationTool.class);

    private final List<NotificationRecord> sentLog = new CopyOnWriteArrayList<>();

    @Tool(description = "Send a notification message via a specified channel. " +
            "Supported channels: 'email', 'slack', 'sms'. " +
            "Returns a confirmation with a tracking ID.")
    public String sendNotification(
            @ToolParam(description = "Channel to send through: email, slack, or sms") String channel,
            @ToolParam(description = "Recipient (email address, Slack channel like #general, or phone number)") String recipient,
            @ToolParam(description = "Subject or title of the notification") String subject,
            @ToolParam(description = "Body/content of the notification message") String body) {

        String ch = channel.toLowerCase().trim();
        log.debug("sendNotification() — channel: '{}', recipient: '{}', subject: '{}'", ch, recipient, subject);
        String trackingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        String validationError = validateChannel(ch, recipient);
        if (validationError != null) {
            log.warn("sendNotification() — validation failed for channel '{}', recipient '{}': {}",
                    ch, recipient, validationError);
            return validationError;
        }

        var record = new NotificationRecord(trackingId, ch, recipient, subject, body, now, "DELIVERED");
        sentLog.add(record);
        log.info("sendNotification() — sent via {} to '{}', trackingId: {}", ch.toUpperCase(), recipient, trackingId);

        String channelEmoji = switch (ch) {
            case "email" -> "📧";
            case "slack" -> "💬";
            case "sms" -> "📱";
            default -> "📨";
        };

        return String.format("""
                %s Notification Sent Successfully
                
                Tracking ID: %s
                Channel: %s
                Recipient: %s
                Subject: %s
                Timestamp: %s
                Status: ✅ DELIVERED
                
                Preview:
                ---
                %s
                ---
                """,
                channelEmoji, trackingId, ch.toUpperCase(), recipient, subject,
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                body.length() > 200 ? body.substring(0, 200) + "..." : body);
    }

    @Tool(description = "Send a notification to multiple recipients at once. " +
            "Useful for team-wide announcements or incident alerts.")
    public String sendBulkNotification(
            @ToolParam(description = "Channel: email, slack, or sms") String channel,
            @ToolParam(description = "Comma-separated list of recipients") String recipients,
            @ToolParam(description = "Subject of the notification") String subject,
            @ToolParam(description = "Body of the notification") String body) {

        String ch = channel.toLowerCase().trim();
        String[] recipientList = recipients.split(",");
        log.info("sendBulkNotification() — channel: '{}', recipients: {}, subject: '{}'",
                ch, recipientList.length, subject);
        StringBuilder sb = new StringBuilder();
        sb.append("📤 Bulk Notification Report\n\n");
        sb.append("| # | Recipient | Tracking ID | Status |\n");
        sb.append("|---|-----------|-------------|--------|\n");

        int success = 0;
        for (int i = 0; i < recipientList.length; i++) {
            String recipient = recipientList[i].trim();
            String trackingId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String validationError = validateChannel(ch, recipient);

            if (validationError != null) {
                log.warn("sendBulkNotification() — invalid recipient '{}' on channel '{}': {}",
                        recipient, ch, validationError);
                sb.append(String.format("| %d | %s | — | ❌ Invalid |\n", i + 1, recipient));
            } else {
                sentLog.add(new NotificationRecord(trackingId, ch, recipient, subject, body, LocalDateTime.now(), "DELIVERED"));
                log.debug("sendBulkNotification() — sent to '{}', trackingId: {}", recipient, trackingId);
                sb.append(String.format("| %d | %s | %s | ✅ Sent |\n", i + 1, recipient, trackingId));
                success++;
            }
        }

        log.info("sendBulkNotification() — completed: {}/{} delivered successfully", success, recipientList.length);
        sb.append(String.format("\n**Summary:** %d/%d delivered successfully\n", success, recipientList.length));
        return sb.toString();
    }

    @Tool(description = "View the notification delivery log. Shows recent notifications " +
            "that were sent, with tracking IDs and delivery status.")
    public String getNotificationLog(
            @ToolParam(description = "Maximum number of recent entries to show (default 10)") int limit) {

        int effectiveLimit = Math.min(Math.max(limit, 1), 50);
        log.debug("getNotificationLog() — requested limit: {}, effective: {}, total logged: {}",
                limit, effectiveLimit, sentLog.size());

        if (sentLog.isEmpty()) {
            log.debug("getNotificationLog() — log is empty");
            return "📋 Notification log is empty. No notifications have been sent yet.";
        }

        log.info("getNotificationLog() — returning up to {} of {} total entries", effectiveLimit, sentLog.size());

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Notification Delivery Log\n\n");
        sb.append("| # | Tracking ID | Channel | Recipient | Subject | Time | Status |\n");
        sb.append("|---|-------------|---------|-----------|---------|------|--------|\n");

        List<NotificationRecord> recent = sentLog.subList(
                Math.max(0, sentLog.size() - effectiveLimit), sentLog.size());

        int i = 1;
        for (NotificationRecord record : recent.reversed()) {
            sb.append(String.format("| %d | %s | %s | %s | %s | %s | %s |\n",
                    i++, record.trackingId, record.channel.toUpperCase(),
                    truncate(record.recipient, 20),
                    truncate(record.subject, 25),
                    record.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    record.status.equals("DELIVERED") ? "✅" : "❌"));
        }

        sb.append(String.format("\nTotal notifications sent: %d\n", sentLog.size()));
        return sb.toString();
    }

    private String validateChannel(String channel, String recipient) {
        return switch (channel) {
            case "email" -> {
                if (!recipient.contains("@")) yield "❌ Invalid email address: " + recipient + ". Must contain @.";
                yield null;
            }
            case "slack" -> {
                if (!recipient.startsWith("#") && !recipient.startsWith("@"))
                    yield "❌ Invalid Slack recipient: " + recipient + ". Use #channel or @user format.";
                yield null;
            }
            case "sms" -> {
                if (!recipient.matches(".*\\d{7,}.*"))
                    yield "❌ Invalid phone number: " + recipient + ". Must contain at least 7 digits.";
                yield null;
            }
            default -> "❌ Unsupported channel: " + channel + ". Use email, slack, or sms.";
        };
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private record NotificationRecord(
            String trackingId, String channel, String recipient,
            String subject, String body, LocalDateTime timestamp, String status) {
    }
}

