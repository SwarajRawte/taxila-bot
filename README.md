# Taxila Notification Bot

A 24/7 cloud-deployable Java bot that logs into the BITS Taxila portal, checks for new notifications periodically, and sends alerts to Telegram.

## Features

✅ Automated login to BITS Taxila portal  
✅ Periodic notification checking (configurable interval)  
✅ Telegram bot integration for real-time alerts  
✅ Duplicate notification prevention  
✅ Headless browser mode (cloud-ready)  
✅ Robust error handling and retry logic  
✅ Comprehensive logging system  
✅ Easy environment variable configuration  

## Technology Stack

- **Java 11+** - Main programming language
- **Selenium WebDriver 4.15** - Browser automation
- **JSoup 1.16** - HTML parsing
- **WebDriverManager** - Automatic ChromeDriver management
- **Telegram Bot API** - Notification delivery
- **Logback** - Logging framework
- **Maven** - Build automation
- **Chrome/Chromium** - Headless browser engine

## Prerequisites

### Local Development
- Java 11 or higher
- Maven 3.6 or higher
- Chrome or Chromium browser
- Telegram Bot Token
- BITS Taxila login credentials

### Cloud Deployment (Render)
- Git repository
- Render account
- Environment variables configured

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd elern_bot
```

### 2. Set Up Environment Variables

**Local Development:**
```bash
# Copy example file
cp .env.example .env

# Edit .env with your credentials
nano .env
```

**Required Variables:**
```
TAXILA_USERNAME=your_bits_rollnumber
TAXILA_PASSWORD=your_taxila_password
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
```

**Optional Variables:**
```
CHECK_INTERVAL_MINUTES=10          # Default: 10 minutes
HEADLESS_MODE=true                  # Default: true
DATA_DIRECTORY=./data               # Default: ./data
```

### 3. Build the Project

```bash
mvn clean package
```

This creates the JAR file: `target/taxila-notification-bot-1.0.0.jar`

### 4. Run Locally

**Using Maven:**
```bash
mvn clean compile exec:java -Dexec.mainClass="com.taxilabot.Main"
```

**Using JAR:**
```bash
java -jar target/taxila-notification-bot-1.0.0.jar
```

## How to Get Telegram Bot Token

1. Open Telegram and search for `@BotFather`
2. Start a chat and send `/start`
3. Send `/newbot` command
4. Follow the prompts to create a bot
5. Copy the **Bot Token** (format: `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)
6. Send a message to your new bot
7. In browser, visit: `https://api.telegram.org/bot<TOKEN>/getUpdates`
8. Find your **Chat ID** in the response (look for "id" field)

## Configuration Details

### Check Interval
- Controls how often the bot checks for new notifications
- Default: 10 minutes
- Minimum: 1 minute (not recommended)
- Use higher values to reduce server load

### Headless Mode
- Runs browser without GUI (required for servers)
- Default: true (always enabled for cloud)
- Improves performance and reduces memory usage

### Data Directory
- Stores notification history (prevents duplicates)
- Default: `./data`
- Must be writable
- Creates `notifications_history.txt` file

## Project Structure

```
elern_bot/
├── src/main/
│   ├── java/com/taxilabot/
│   │   ├── Main.java                      # Entry point
│   │   ├── bot/
│   │   │   └── TaxilaBot.java            # Main automation logic
│   │   ├── model/
│   │   │   └── NotificationData.java     # Notification object
│   │   ├── notify/
│   │   │   └── TelegramNotifier.java     # Telegram integration
│   │   └── util/
│   │       ├── BotLogger.java            # Logging utility
│   │       ├── ConfigManager.java        # Environment config
│   │       └── NotificationTracker.java  # Notification history
│   └── resources/
│       └── logback.xml                    # Logging configuration
├── pom.xml                                # Maven configuration
├── Procfile                               # Cloud deployment config
├── .env.example                           # Environment template
└── README.md                              # This file
```

## Logging

### Log Output
- **Console:** Real-time output to terminal
- **Files:** Stored in `./logs/taxila-bot.log`
- **Log Rotation:** Automatic rotation at 10MB
- **Retention:** 30 days or 1GB total

### Log Levels
- `DEBUG` - Detailed information for debugging
- `INFO` - General operational messages
- `WARN` - Warning messages
- `ERROR` - Error messages with stack traces

### Viewing Logs

```bash
# View current log
tail -f logs/taxila-bot.log

# View all logs
ls -la logs/
```

## Troubleshooting

### Bot Not Sending Notifications

1. **Check Telegram credentials:**
   ```bash
   echo $TELEGRAM_BOT_TOKEN
   echo $TELEGRAM_CHAT_ID
   ```

2. **Test Telegram connection manually:**
   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/sendMessage?chat_id=<CHAT_ID>&text=Test"
   ```

3. **Check logs for errors:**
   ```bash
   tail -f logs/taxila-bot.log
   ```

### Login Failures

1. **Verify credentials:**
   - Username: Your BITS Roll Number (e.g., 2022A1PS0123G)
   - Password: Your Taxila portal password

2. **Check if credentials have special characters:**
   - If yes, ensure they're properly URL-encoded

3. **Test manual login:**
   - Visit: https://taxila-aws.bits-pilani.ac.in/login/index.php
   - Try logging in directly

### Bot Running But No Notifications

1. **Check if browser automation is working:**
   - Look for DEBUG logs showing page loads

2. **Verify notification HTML structure:**
   - The bot uses CSS selectors that may differ from actual HTML
   - Check the extracted HTML in logs

3. **Increase check interval for testing:**
   - Set `CHECK_INTERVAL_MINUTES=2` for faster testing

### Memory Issues

1. **Reduce browser memory usage:**
   - Already using headless mode (default)
   - Disable images in Chrome options (enabled)

2. **For cloud deployment:**
   - Use Render's paid plans for more memory
   - Monitor resource usage in dashboard

## Cloud Deployment (Render)

### Step 1: Push to GitHub

```bash
git add .
git commit -m "Initial commit - Taxila Notification Bot"
git push origin main
```

### Step 2: Create Render Account

Visit: https://render.com

### Step 3: Create Background Worker

1. Log in to Render dashboard
2. Click **New +** → **Background Worker**
3. Select your GitHub repository
4. Configure:
   - **Name:** taxila-notification-bot
   - **Environment:** Docker
   - **Build Command:** `mvn clean package`
   - **Start Command:** `java -jar target/taxila-notification-bot-1.0.0.jar`

### Step 4: Set Environment Variables

In Render dashboard:
1. Go to your service → **Environment**
2. Add variables:
   ```
   TAXILA_USERNAME=your_bits_rollnumber
   TAXILA_PASSWORD=your_taxila_password
   TELEGRAM_BOT_TOKEN=your_bot_token
   TELEGRAM_CHAT_ID=your_chat_id
   ```

### Step 5: Deploy

1. Click **Deploy** in Render dashboard
2. Monitor logs in **Logs** tab
3. Bot runs 24/7 automatically

### Step 6: Monitor

- Check service status: https://render.com/dashboard
- View logs in real-time
- Monitor resource usage

## Advanced Configuration

### Custom Check Interval

For production use:
```bash
export CHECK_INTERVAL_MINUTES=15
java -jar target/taxila-notification-bot-1.0.0.jar
```

### Custom Data Directory

```bash
export DATA_DIRECTORY=/var/lib/taxila-bot/data
java -jar target/taxila-notification-bot-1.0.0.jar
```

### Running as Background Service (Linux)

Create `/etc/systemd/system/taxila-bot.service`:
```ini
[Unit]
Description=Taxila Notification Bot
After=network.target

[Service]
Type=simple
User=taxila
WorkingDirectory=/opt/taxila-bot
EnvironmentFile=/opt/taxila-bot/.env
ExecStart=/usr/bin/java -jar /opt/taxila-bot/target/taxila-notification-bot-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable taxila-bot
sudo systemctl start taxila-bot
sudo systemctl status taxila-bot
```

## Performance Optimization

### Bottlenecks and Solutions

| Issue | Solution |
|-------|----------|
| Slow page loads | Increase WAIT_TIMEOUT in TaxilaBot.java |
| High memory usage | Use smaller check intervals on limited systems |
| Failed logins | Add delays between login attempts |
| Telegram rate limits | Verify chat ID and bot token |

### Best Practices

1. **Check Interval:**
   - 10-15 minutes: Good balance for school notifications
   - 5 minutes: More frequent, higher resource usage
   - 30 minutes+: Less frequent, reduced load

2. **Error Recovery:**
   - Bot automatically retries on failures
   - Failed checks don't stop the loop
   - Driver resources cleaned up automatically

3. **Notifications:**
   - Failed to send notifications are logged
   - Bot continues running despite failures
   - Retries up to 3 times before giving up

## API Reference

### Telegram API

The bot uses Telegram Bot API for sending messages:
- **Endpoint:** `https://api.telegram.org/bot<TOKEN>/sendMessage`
- **Method:** HTTP GET
- **Parameters:**
  - `chat_id`: Your chat ID
  - `text`: Message text
  - `parse_mode`: HTML for formatting

### Selenium WebDriver

Used for browser automation:
- Opens BITS Taxila portal
- Fills login credentials
- Navigates to notifications page
- Extracts HTML content

### JSoup

HTML parsing library:
- Parses notification elements
- Extracts titles and timestamps
- Handles malformed HTML gracefully

## Security Considerations

⚠️ **Important:**
1. Never commit `.env` file with real credentials to GitHub
2. Use `.gitignore` to exclude `.env` and `data/` directory
3. Use environment variables for sensitive data
4. Rotate Telegram bot token if compromised
5. Change BITS password periodically
6. Review logs regularly for suspicious activity

### Example .gitignore

```
.env
data/
logs/
target/
*.class
.DS_Store
.idea/
*.iml
```

## Debugging

### Enable Debug Logging

Edit `src/main/resources/logback.xml`:
```xml
<logger name="com.taxilabot" level="DEBUG" />
```

Rebuild:
```bash
mvn clean package
```

### Debug Mode Execution

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG -jar target/taxila-notification-bot-1.0.0.jar
```

### Common Error Messages

| Error | Cause | Fix |
|-------|-------|-----|
| `Missing required environment variable` | Config not set | Check `.env` file |
| `Login timeout` | Portal not responding or credentials wrong | Verify login manually |
| `Telegram API error` | Invalid token or chat ID | Check bot token and chat ID |
| `WebDriver initialization failed` | Chrome not installed | Install Chrome/Chromium |

## Contributing

To add features or fix bugs:

1. Fork the repository
2. Create a feature branch
3. Make changes
4. Test locally
5. Submit pull request

## License

This project is provided as-is for BITS Pilani students.

## Support

For issues and questions:
1. Check logs: `logs/taxila-bot.log`
2. Review troubleshooting section
3. Verify environment variables
4. Check GitHub Issues

## Future Enhancements

- [ ] Database integration for better notification history
- [ ] Web dashboard for bot status monitoring
- [ ] Discord bot integration
- [ ] Email notifications
- [ ] Notification filtering and categorization
- [ ] Multiple user support
- [ ] Mobile app for alerts

---

**Last Updated:** March 2026  
**Version:** 1.0.0  
**Status:** Production Ready
