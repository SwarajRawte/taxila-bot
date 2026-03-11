package com.taxilabot.model;

import com.google.gson.Gson;

/**
 * Represents a notification from the Taxila portal
 * Used for tracking and comparing notifications
 */
public class NotificationData {
    private String title;
    private String message;
    private String timestamp;
    private String source;
    private String uniqueId;

    public NotificationData(String title, String message, String timestamp, String source) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.source = source;
        // Create unique ID based on title and timestamp
        this.uniqueId = generateUniqueId();
    }

    /**
     * Generate unique ID for the notification
     * Used to avoid duplicate processing
     */
    private String generateUniqueId() {
        String combined = (title != null ? title : "") + "_" + (timestamp != null ? timestamp : "");
        return Integer.toHexString(combined.hashCode());
    }

    /**
     * Convert to JSON string for storage
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Create instance from JSON string
     */
    public static NotificationData fromJson(String json) {
        return new Gson().fromJson(json, NotificationData.class);
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "NotificationData{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NotificationData other = (NotificationData) obj;
        return uniqueId != null && uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId != null ? uniqueId.hashCode() : 0;
    }
}
