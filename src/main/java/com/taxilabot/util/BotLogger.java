package com.taxilabot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging utility for the bot
 * Provides consistent logging across all components
 */
public class BotLogger {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static class LoggerHolder {
        private final String className;
        private final Logger logger;

        LoggerHolder(Class<?> clazz) {
            this.className = clazz.getSimpleName();
            this.logger = LoggerFactory.getLogger(clazz);
        }
    }

    private final LoggerHolder holder;

    private BotLogger(Class<?> clazz) {
        this.holder = new LoggerHolder(clazz);
    }

    public static BotLogger getLogger(Class<?> clazz) {
        return new BotLogger(clazz);
    }

    private String formatMessage(String message) {
        String timestamp = LocalDateTime.now().format(dateFormatter);
        return "[" + timestamp + "] [" + holder.className + "] " + message;
    }

    public void info(String message) {
        holder.logger.info(formatMessage(message));
    }

    public void debug(String message) {
        holder.logger.debug(formatMessage(message));
    }

    public void warn(String message) {
        holder.logger.warn(formatMessage(message));
    }

    public void error(String message, Throwable throwable) {
        holder.logger.error(formatMessage(message), throwable);
    }

    public void error(String message) {
        holder.logger.error(formatMessage(message));
    }
}
