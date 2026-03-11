package com.taxilabot.util;

import com.taxilabot.model.NotificationData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Tracks previously seen notifications
 * Stores notification IDs in a local file to avoid duplicate alerts
 */
public class NotificationTracker {
    private static final BotLogger logger = BotLogger.getLogger(NotificationTracker.class);
    private static final String NOTIFICATIONS_FILE = "notifications_history.txt";
    private final ConfigManager config;
    private final Set<String> seenNotificationIds;
    private final File dataDir;
    private final File historyFile;

    public NotificationTracker() {
        this.config = ConfigManager.getInstance();
        this.seenNotificationIds = new HashSet<>();
        this.dataDir = new File(config.getDataDirectory());
        this.historyFile = new File(dataDir, NOTIFICATIONS_FILE);

        // Initialize data directory
        initializeDataDirectory();

        // Load existing notifications from file
        loadNotificationsFromFile();
    }

    /**
     * Initialize data directory if it doesn't exist
     */
    private void initializeDataDirectory() {
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                logger.info("Data directory created: " + dataDir.getAbsolutePath());
            } else {
                logger.warn("Failed to create data directory: " + dataDir.getAbsolutePath());
            }
        }
    }

    /**
     * Load previously seen notification IDs from file
     */
    private void loadNotificationsFromFile() {
        try {
            if (historyFile.exists()) {
                List<String> lines = Files.readAllLines(
                    historyFile.toPath(),
                    StandardCharsets.UTF_8
                );
                seenNotificationIds.addAll(lines);
                logger.info("Loaded " + seenNotificationIds.size() + 
                           " previously seen notifications from file");
            } else {
                logger.info("Notification history file not found. Starting fresh.");
            }
        } catch (IOException e) {
            logger.error("Error loading notification history", e);
            logger.warn("Starting fresh notification tracking");
        }
    }

    /**
     * Check if notification is new (not seen before)
     */
    public boolean isNewNotification(NotificationData notification) {
        return !seenNotificationIds.contains(notification.getUniqueId());
    }

    /**
     * Mark notification as seen (save to file and memory)
     */
    public void markAsSeen(NotificationData notification) {
        String id = notification.getUniqueId();
        if (!seenNotificationIds.contains(id)) {
            seenNotificationIds.add(id);
            saveNotificationToFile(id);
            logger.debug("Marked notification as seen: " + id);
        }
    }

    /**
     * Save notification ID to file
     */
    private void saveNotificationToFile(String notificationId) {
        try {
            // Append to file
            try (FileWriter writer = new FileWriter(historyFile, true)) {
                writer.write(notificationId + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            logger.error("Error saving notification to file", e);
        }
    }

    /**
     * Get count of tracked notifications
     */
    public int getTrackedCount() {
        return seenNotificationIds.size();
    }

    /**
     * Clear all notification history (use with caution)
     */
    public void clearHistory() {
        try {
            seenNotificationIds.clear();
            if (historyFile.delete()) {
                logger.info("Notification history cleared");
            }
        } catch (Exception e) {
            logger.error("Error clearing notification history", e);
        }
    }
}
