package com.example.mcpserver.tools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * Unit tests for NotificationTool.
 */
class NotificationToolTest {
    private NotificationTool tool;
    @BeforeEach
    void setUp() {
        tool = new NotificationTool();
    }
    @Test
    void sendNotification_validEmail_returnsDelivered() {
        String result = tool.sendNotification("email", "user@example.com", "Hello", "Body");
        assertThat(result).contains("DELIVERED").contains("user@example.com").contains("Tracking ID");
    }
    @Test
    void sendNotification_emailWithoutAtSign_returnsValidationError() {
        String result = tool.sendNotification("email", "notanemail", "Hello", "Body");
        assertThat(result).contains("Invalid email");
    }
    @Test
    void sendNotification_validSlackChannel_returnsDelivered() {
        String result = tool.sendNotification("slack", "#general", "Alert", "System update");
        assertThat(result).contains("DELIVERED").contains("#general");
    }
    @Test
    void sendNotification_validSlackUser_returnsDelivered() {
        String result = tool.sendNotification("slack", "@alice", "Hi", "DM");
        assertThat(result).contains("DELIVERED");
    }
    @Test
    void sendNotification_invalidSlackRecipient_returnsError() {
        String result = tool.sendNotification("slack", "no-prefix", "Hi", "Body");
        assertThat(result).contains("Invalid Slack recipient");
    }
    @Test
    void sendNotification_validPhoneNumber_returnsDelivered() {
        String result = tool.sendNotification("sms", "+1555000123", "Alert", "Critical");
        assertThat(result).contains("DELIVERED");
    }
    @Test
    void sendNotification_phoneWithTooFewDigits_returnsError() {
        String result = tool.sendNotification("sms", "123", "Alert", "Body");
        assertThat(result).contains("Invalid phone number");
    }
    @Test
    void sendNotification_unsupportedChannel_returnsError() {
        String result = tool.sendNotification("fax", "user@example.com", "Hello", "Body");
        assertThat(result).contains("Unsupported channel");
    }
    @ParameterizedTest
    @ValueSource(strings = {"EMAIL", "Email", "email"})
    void sendNotification_channelCaseInsensitive(String channel) {
        String result = tool.sendNotification(channel, "user@example.com", "Test", "Body");
        assertThat(result).contains("DELIVERED");
    }
    @Test
    void sendNotification_containsTrackingId() {
        String result = tool.sendNotification("slack", "#ops", "Deploy", "v2");
        assertThat(result).containsPattern("[A-Z0-9]{8}");
    }
    @Test
    void sendNotification_longBody_isTruncatedInPreview() {
        String result = tool.sendNotification("email", "a@b.com", "Subject", "A".repeat(300));
        assertThat(result).contains("...");
    }
    @Test
    void sendNotification_shortBody_notTruncated() {
        String result = tool.sendNotification("email", "a@b.com", "Subject", "Short body");
        assertThat(result).contains("Short body").doesNotContain("...");
    }
    @Test
    void sendBulkNotification_multipleRecipients_sendsToAll() {
        String result = tool.sendBulkNotification("email", "a@x.com, b@x.com, c@x.com", "Announcement", "Hello");
        assertThat(result).contains("3/3 delivered");
    }
    @Test
    void sendBulkNotification_mixedValidAndInvalid_reportsCorrectCount() {
        String result = tool.sendBulkNotification("email", "valid@x.com, notanemail, another@x.com", "Test", "Body");
        assertThat(result).contains("2/3 delivered");
    }
    @Test
    void sendBulkNotification_containsMarkdownTable() {
        String result = tool.sendBulkNotification("slack", "#ch1, #ch2", "Hi", "Body");
        assertThat(result).contains("| # |").contains("| Recipient |").contains("| Status |");
    }
    @Test
    void getNotificationLog_emptyLog_returnsEmptyMessage() {
        assertThat(tool.getNotificationLog(10)).contains("empty");
    }
    @Test
    void getNotificationLog_afterSending_showsEntries() {
        tool.sendNotification("email", "a@b.com", "Subject", "Body");
        String result = tool.getNotificationLog(10);
        assertThat(result).contains("EMAIL").contains("Total notifications sent: 1");
    }
    @Test
    void getNotificationLog_respectsLimit() {
        for (int i = 0; i < 20; i++) {
            tool.sendNotification("slack", "#ch", "Msg", "Body");
        }
        assertThat(tool.getNotificationLog(5)).contains("Total notifications sent: 20");
    }
    @Test
    void getNotificationLog_clampsExtremeValues() {
        assertThat(tool.getNotificationLog(999)).isNotNull();
        assertThat(tool.getNotificationLog(0)).isNotNull();
    }
    @Test
    void sentLog_isIsolatedPerInstance() {
        NotificationTool toolA = new NotificationTool();
        NotificationTool toolB = new NotificationTool();
        toolA.sendNotification("email", "a@b.com", "S", "B");
        assertThat(toolA.getNotificationLog(10)).contains("Total notifications sent: 1");
        assertThat(toolB.getNotificationLog(10)).contains("empty");
    }
}
