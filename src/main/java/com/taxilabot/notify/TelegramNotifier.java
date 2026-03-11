package com.taxilabot.notify;

import com.taxilabot.model.NotificationData;
import com.taxilabot.util.BotLogger;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sends notifications to Telegram bot
 * Handles API communication with Telegram messaging service
 */
public class TelegramNotifier {
    private static final BotLogger logger = BotLogger.getLogger(TelegramNotifier.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_ENDPOINT = "/sendMessage";
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String botToken;
    private final String chatId;
    private final CloseableHttpClient httpClient;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Send notification via Telegram
     */
    public boolean sendNotification(NotificationData notification) {
        try {
            String message = formatMessage(notification);
            return sendMessage(message);
        } catch (Exception e) {
            logger.error("Error sending notification to Telegram", e);
            return false;
        }
    }

    /**
     * Send raw text message via Telegram
     */
    public boolean sendMessage(String text) {
        CloseableHttpClient client = HttpClients.createDefault();
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                String url = buildTelegramUrl(text);
                HttpGet httpGet = new HttpGet(url);

                int statusCode = client.execute(httpGet, response -> {
                    int code = response.getStatusLine().getStatusCode();
                    EntityUtils.consume(response.getEntity());
                    return code;
                });

                if (statusCode == 200) {
                    logger.info("Message sent successfully to Telegram");
                    return true;
                } else {
                    logger.warn("Telegram API returned status code: " + statusCode);
                    retryCount++;
                    if (retryCount < maxRetries) {
                        logger.info("Retrying... (attempt " + (retryCount + 1) + ")");
                        Thread.sleep(1000); // Wait before retry
                    }
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while sending message", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error sending message to Telegram (attempt " + (retryCount + 1) + ")", e);
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(2000); // Wait before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Build Telegram API URL with message
     */
    private String buildTelegramUrl(String text) throws Exception {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        return TELEGRAM_API_BASE + botToken + SEND_MESSAGE_ENDPOINT +
               "?chat_id=" + chatId +
               "&text=" + encodedText +
               "&parse_mode=HTML";
    }

    /**
     * Format notification for Telegram message
     */
    private String formatMessage(NotificationData notification) {
        StringBuilder message = new StringBuilder();
        message.append("<b>🔔 New Notification from BITS Taxila</b>\n");
        message.append("─".repeat(40)).append("\n\n");

        if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
            message.append("<b>📌 Title:</b>\n");
            message.append(escapeHtml(notification.getTitle())).append("\n\n");
        }

        if (notification.getMessage() != null && !notification.getMessage().isEmpty()) {
            message.append("<b>📝 Message:</b>\n");
            message.append(escapeHtml(notification.getMessage())).append("\n\n");
        }

        if (notification.getTimestamp() != null && !notification.getTimestamp().isEmpty()) {
            message.append("<b>⏰ Time:</b> ");
            message.append(escapeHtml(notification.getTimestamp())).append("\n\n");
        }

        message.append("<b>🔔 Alert Time:</b> ");
        message.append(LocalDateTime.now().format(timeFormatter)).append("\n");
        message.append("─".repeat(40));

        return message.toString();
    }

    /**
     * Escape HTML special characters for Telegram
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /**
     * Test Telegram connection
     */
    public boolean testConnection() {
        try {
            return sendMessage("✅ <b>Taxila Bot Connected</b>\nBot is ready to send notifications!");
        } catch (Exception e) {
            logger.error("Telegram connection test failed", e);
            return false;
        }
    }

    /**
     * Close HTTP client resources
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}
