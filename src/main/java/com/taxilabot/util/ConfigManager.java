package com.taxilabot.util;

/**
 * Manages application configuration from environment variables
 * Validates required configurations on startup
 */
public class ConfigManager {
    private static final BotLogger logger = BotLogger.getLogger(ConfigManager.class);
    private static ConfigManager instance;

    private final String taxilaUsername;
    private final String taxilaPassword;
    private final String telegramBotToken;
    private final String telegramChatId;
    private final int checkIntervalMinutes;
    private final boolean headlessMode;
    private final String dataDirectory;

    private ConfigManager() {
        this.taxilaUsername = getEnvVariable("TAXILA_USERNAME", true);
        this.taxilaPassword = getEnvVariable("TAXILA_PASSWORD", true);
        this.telegramBotToken = getEnvVariable("TELEGRAM_BOT_TOKEN", true);
        this.telegramChatId = getEnvVariable("TELEGRAM_CHAT_ID", true);
        this.checkIntervalMinutes = Integer.parseInt(
            getEnvVariable("CHECK_INTERVAL_MINUTES", false, "10")
        );
        this.headlessMode = Boolean.parseBoolean(
            getEnvVariable("HEADLESS_MODE", false, "true")
        );
        this.dataDirectory = getEnvVariable("DATA_DIRECTORY", false, "./data");
    }

    /**
     * Get singleton instance of ConfigManager
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Validate all required configurations
     * Throws exception if critical config is missing
     */
    public void validate() throws IllegalStateException {
        StringBuilder errors = new StringBuilder();

        if (taxilaUsername == null || taxilaUsername.isEmpty()) {
            errors.append("TAXILA_USERNAME is required\n");
        }
        if (taxilaPassword == null || taxilaPassword.isEmpty()) {
            errors.append("TAXILA_PASSWORD is required\n");
        }
        if (telegramBotToken == null || telegramBotToken.isEmpty()) {
            errors.append("TELEGRAM_BOT_TOKEN is required\n");
        }
        if (telegramChatId == null || telegramChatId.isEmpty()) {
            errors.append("TELEGRAM_CHAT_ID is required\n");
        }
        if (checkIntervalMinutes < 1) {
            errors.append("CHECK_INTERVAL_MINUTES must be >= 1\n");
        }

        if (errors.length() > 0) {
            throw new IllegalStateException("Configuration validation failed:\n" + errors.toString());
        }

        logger.info("Configuration validated successfully");
    }

    /**
     * Get environment variable with fallback
     */
    private String getEnvVariable(String key, boolean required) {
        String value = System.getenv(key);
        if (required && (value == null || value.isEmpty())) {
            logger.error("Missing required environment variable: " + key);
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    /**
     * Get environment variable with default value
     */
    private String getEnvVariable(String key, boolean required, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            if (required) {
                throw new IllegalStateException("Missing required environment variable: " + key);
            }
            logger.info("Using default for " + key + ": " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    // Getters
    public String getTaxilaUsername() {
        return taxilaUsername;
    }

    public String getTaxilaPassword() {
        return taxilaPassword;
    }

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public int getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public boolean isHeadlessMode() {
        return headlessMode;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }
}
