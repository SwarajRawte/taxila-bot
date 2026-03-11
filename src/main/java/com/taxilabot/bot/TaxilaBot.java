package com.taxilabot.bot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.taxilabot.model.NotificationData;
import com.taxilabot.notify.TelegramNotifier;
import com.taxilabot.util.BotLogger;
import com.taxilabot.util.NotificationTracker;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Main bot class for BITS Taxila portal automation
 * Logs in, extracts notifications, and sends alerts via Telegram
 */
public class TaxilaBot {
    private static final BotLogger logger = BotLogger.getLogger(TaxilaBot.class);

    // Login page URL
    private static final String TAXILA_LOGIN_URL = "https://taxila-aws.bits-pilani.ac.in/login/index.php";
    private static final String TAXILA_HOME_URL = "https://taxila-aws.bits-pilani.ac.in/my/";
    private static final String TAXILA_NOTIFICATIONS_URL = "https://taxila-aws.bits-pilani.ac.in/message/output/";

    // Selenium timeout in seconds
    private static final int WAIT_TIMEOUT = 20;

    private final String username;
    private final String password;
    private final TelegramNotifier telegramNotifier;
    private final NotificationTracker notificationTracker;
    private final int checkIntervalMinutes;
    private final boolean headlessMode;

    private WebDriver driver;

    public TaxilaBot(String username, String password, String telegramToken,
                     String telegramChatId, int checkIntervalMinutes) {
        this.username = username;
        this.password = password;
        this.telegramNotifier = new TelegramNotifier(telegramToken, telegramChatId);
        this.notificationTracker = new NotificationTracker();
        this.checkIntervalMinutes = checkIntervalMinutes;
        this.headlessMode = true; // Always use headless mode for cloud deployment

        logger.info("TaxilaBot initialized");
    }

    /**
     * Start the bot (infinite loop)
     */
    public void start() {
        try {
            // Test Telegram connection on startup
            logger.info("Testing Telegram connection...");
            if (telegramNotifier.testConnection()) {
                logger.info("Telegram connection successful!");
            } else {
                logger.warn("Telegram connection test failed, but continuing...");
            }

            // Main loop
            long checkIntervalMs = (long) checkIntervalMinutes * 60 * 1000;
            int iterationCount = 0;

            while (true) {
                iterationCount++;
                logger.info("=" .repeat(50));
                logger.info("Iteration #" + iterationCount + " - Starting notification check");
                logger.info("Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                try {
                    // Perform check
                    checkNotifications();
                    logger.info("Check completed successfully");

                } catch (Exception e) {
                    logger.error("Error during notification check", e);
                    // Close driver on error and reinitialize on next iteration
                    closeDriver();
                }

                // Wait for next check
                logger.info("Waiting " + checkIntervalMinutes + " minutes before next check...");
                Thread.sleep(checkIntervalMs);
            }

        } catch (InterruptedException e) {
            logger.error("Bot interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            closeDriver();
            telegramNotifier.close();
            logger.info("Bot stopped");
        }
    }

    /**
     * Main logic: check for new notifications
     */
    private void checkNotifications() {
        try {
            // Initialize driver
            initializeDriver();

            // Login to Taxila
            logger.info("Logging into Taxila portal...");
            loginToTaxila();

            // Navigate to notifications
            logger.info("Navigating to notifications...");
            navigateToNotifications();

            // Wait for page to load
            Thread.sleep(2000);

            // Get current page source
            String pageSource = driver.getPageSource();

            // Parse HTML
            Document doc = Jsoup.parse(pageSource);

            // Extract notifications
            List<NotificationData> notifications = extractNotifications(doc);

            logger.info("Found " + notifications.size() + " notifications on page");

            // Process notifications
            int newNotifications = 0;
            for (NotificationData notification : notifications) {
                if (notificationTracker.isNewNotification(notification)) {
                    logger.info("New notification detected: " + notification.getTitle());

                    // Send to Telegram
                    if (telegramNotifier.sendNotification(notification)) {
                        notificationTracker.markAsSeen(notification);
                        newNotifications++;
                    }
                } else {
                    logger.debug("Notification already seen: " + notification.getTitle());
                }
            }

            logger.info("Processed " + newNotifications + " new notifications");

        } catch (Exception e) {
            logger.error("Error checking notifications", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize WebDriver
     */
    private void initializeDriver() {
        try {
            if (driver != null) {
                return; // Already initialized
            }

            logger.debug("Initializing Chrome WebDriver...");

            // Setup WebDriver
            WebDriverManager.chromedriver().setup();

            // Configure Chrome options
            ChromeOptions options = new ChromeOptions();

            // Headless mode
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // Disable images and CSS for faster loading
            options.addArguments("--blink-settings=imagesEnabled=false");

            // Window size
            options.addArguments("--window-size=1920,1080");

            // Create driver
            driver = new ChromeDriver(options);

            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(WAIT_TIMEOUT));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(WAIT_TIMEOUT));

            logger.info("WebDriver initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing WebDriver", e);
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    /**
     * Login to Taxila portal
     */
    private void loginToTaxila() {
        try {
            // Navigate to login page
            logger.debug("Navigating to login page...");
            driver.get(TAXILA_LOGIN_URL);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));

            // Find and fill username field
            logger.debug("Entering credentials...");
            WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.name("username"))
            );
            usernameField.clear();
            usernameField.sendKeys(username);

            // Find and fill password field
            WebElement passwordField = driver.findElement(By.name("password"));
            passwordField.clear();
            passwordField.sendKeys(password);

            // Find and click login button
            WebElement loginButton = driver.findElement(By.id("loginbtn"));
            loginButton.click();

            // Wait for login to complete
            logger.debug("Waiting for login to complete...");
            wait.until(ExpectedConditions.urlContains("/my/"));

            logger.info("Login successful");

        } catch (TimeoutException e) {
            logger.error("Login timeout - could not find login elements", e);
            throw new RuntimeException("Login failed", e);
        } catch (Exception e) {
            logger.error("Error during login", e);
            throw new RuntimeException("Login failed", e);
        }
    }

    /**
     * Navigate to notifications page
     */
    private void navigateToNotifications() {
        try {
            logger.debug("Navigating to notifications URL: " + TAXILA_NOTIFICATIONS_URL);
            driver.get(TAXILA_NOTIFICATIONS_URL);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            logger.info("Notifications page loaded");

        } catch (Exception e) {
            logger.error("Error navigating to notifications", e);
            throw new RuntimeException("Failed to navigate to notifications", e);
        }
    }

    /**
     * Extract notifications from HTML
     */
    private List<NotificationData> extractNotifications(Document doc) {
        List<NotificationData> notifications = new ArrayList<>();

        try {
            // Look for notification items (adjust selectors based on actual HTML structure)
            Elements notificationRows = doc.select("div[class*='notification'], tr[class*='message']");

            logger.debug("Found " + notificationRows.size() + " notification elements");

            for (Element row : notificationRows) {
                try {
                    // Extract title
                    String title = extractTitle(row);
                    if (title == null || title.isEmpty()) continue;

                    // Extract message
                    String message = extractMessage(row);

                    // Extract timestamp
                    String timestamp = extractTimestamp(row);

                    // Create notification object
                    NotificationData notification = new NotificationData(
                        title,
                        message,
                        timestamp,
                        "BITS_TAXILA"
                    );

                    notifications.add(notification);
                    logger.debug("Extracted notification: " + title);

                } catch (Exception e) {
                    logger.error("Error extracting individual notification", e);
                }
            }

            // If no specific notification structure found, try generic approach
            if (notifications.isEmpty()) {
                logger.debug("No notifications found using standard selectors, trying generic extraction...");
                notifications.addAll(extractNotificationsGeneric(doc));
            }

        } catch (Exception e) {
            logger.error("Error extracting notifications from HTML", e);
        }

        return notifications;
    }

    /**
     * Extract title from notification element
     */
    private String extractTitle(Element element) {
        // Try multiple selectors
        Element titleElem = element.selectFirst("h3, h4, a[class*='title'], span[class*='title'], strong");
        if (titleElem != null && !titleElem.text().isEmpty()) {
            return titleElem.text().trim();
        }

        // Fallback: get text from element
        String text = element.text();
        if (!text.isEmpty()) {
            // Return first line or first part
            String[] parts = text.split("[\\n\\r]+");
            return parts[0].trim();
        }

        return null;
    }

    /**
     * Extract message from notification element
     */
    private String extractMessage(Element element) {
        Elements paragraphs = element.select("p, div[class*='message'], div[class*='content']");
        if (!paragraphs.isEmpty()) {
            return paragraphs.stream()
                .map(Element::text)
                .filter(text -> !text.isEmpty())
                .findFirst()
                .orElse("");
        }
        return "";
    }

    /**
     * Extract timestamp from notification element
     */
    private String extractTimestamp(Element element) {
        // Look for common timestamp indicators
        Element timeElem = element.selectFirst(
            "time, span[class*='time'], span[class*='date'], .timestamp, .time-label"
        );

        if (timeElem != null && !timeElem.text().isEmpty()) {
            return timeElem.text().trim();
        }

        // Fallback: current time
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Generic notification extraction fallback
     */
    private List<NotificationData> extractNotificationsGeneric(Document doc) {
        List<NotificationData> notifications = new ArrayList<>();

        try {
            // Get all text content and look for patterns
            String bodyText = doc.body().text();

            if (bodyText.length() > 0) {
                // Create a generic notification from body content
                NotificationData notification = new NotificationData(
                    "Taxila Portal Update",
                    bodyText.substring(0, Math.min(500, bodyText.length())),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "BITS_TAXILA"
                );
                notifications.add(notification);
            }

        } catch (Exception e) {
            logger.error("Error in generic extraction", e);
        }

        return notifications;
    }

    /**
     * Close WebDriver
     */
    private void closeDriver() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
                logger.debug("WebDriver closed");
            }
        } catch (Exception e) {
            logger.error("Error closing WebDriver", e);
        }
    }
}
