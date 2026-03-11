package com.taxilabot;

import com.taxilabot.bot.TaxilaBot;
import com.taxilabot.util.BotLogger;
import com.taxilabot.util.ConfigManager;

/**
 * Main entry point for the Taxila Notification Bot
 * Initializes configuration and starts the bot
 */
public class Main {
    private static final BotLogger logger = BotLogger.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("=".repeat(50));
            logger.info("Starting Taxila Notification Bot");
            logger.info("=".repeat(50));

            // Load configuration from environment variables
            ConfigManager config = ConfigManager.getInstance();
            config.validate();

            // Create and start the bot
            TaxilaBot bot = new TaxilaBot(
                config.getTaxilaUsername(),
                config.getTaxilaPassword(),
                config.getTelegramBotToken(),
                config.getTelegramChatId(),
                config.getCheckIntervalMinutes()
            );

            logger.info("Bot configuration loaded successfully");
            logger.info("Check interval: " + config.getCheckIntervalMinutes() + " minutes");

            // Start the bot (infinite loop)
            bot.start();

        } catch (Exception e) {
            logger.error("Fatal error during bot initialization", e);
            System.exit(1);
        }
    }
}
