# Troubleshooting Guide

## Common Issues and Solutions

### 1. Build Errors

#### Error: "cannot find symbol" or "package not found"

**Cause:** Maven dependencies not downloaded

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Force dependency download
mvn clean install -U

# Build again
mvn clean package
```

#### Error: "java.lang.module.FindException"

**Cause:** Java version mismatch

**Solution:**
```bash
# Check Java version
java -version

# Must be Java 11 or higher
javac -version

# If Java 8, install Java 11:
# macOS: brew install openjdk@11
# Ubuntu: sudo apt-get install openjdk-11-jdk
# Windows: Download from oracle.com or adoptopenjdk.net
```

#### Error: "Failed to download plugin or build extension"

**Solution:**
1. Check internet connection
2. Try different Maven repository mirror
3. Edit `pom.xml` to add mirror:

```xml
<repositories>
    <repository>
        <id>central-mirror</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

---

### 2. Login Failures

#### Error: "Login timeout - could not find login elements"

**Possible Causes:**
- Wrong username/password
- Taxila portal is down
- Network issues
- Page structure changed

**Solutions:**
1. **Test credentials manually:**
   - Visit https://taxila-aws.bits-pilani.ac.in/login/index.php
   - Try logging in manually
   - Ensure no 2FA is enabled

2. **Check username format:**
   - Should be your BITS Roll Number (e.g., 2022A1PS0123G)
   - NOT email address

3. **Check password:**
   - Special characters might need escaping
   - Try with simple password first

4. **Increase timeout:**
   ```java
   // In TaxilaBot.java, change:
   private static final int WAIT_TIMEOUT = 30; // Increased from 20
   ```

5. **Check if portal is down:**
   ```bash
   curl -I https://taxila-aws.bits-pilani.ac.in
   ```

#### Error: "org.openqa.selenium.TimeoutException"

**Solution:**
1. Increase WAIT_TIMEOUT in `TaxilaBot.java`
2. Check internet connection
3. Try with headless mode disabled (for debugging)

---

### 3. Notification Issues

#### No Notifications Found

**Possible Causes:**
- Actually no new notifications in portal
- HTML structure doesn't match selectors
- Scraper logic needs adjustment

**Debug Steps:**

1. **Check logs for extracted content:**
   ```bash
   grep "Extracted notification" logs/taxila-bot.log
   ```

2. **Enable debug logging:**
   - Edit `logback.xml`
   - Set level to DEBUG
   - Recompile: `mvn clean package`

3. **Manual HTML inspection:**
   - Open portal in browser
   - Right-click → Inspect → Check HTML structure
   - Update CSS selectors in `extractNotifications()` method

4. **Test scraper locally:**
   ```java
   // Add temporary main method to test scraping
   public static void main(String[] args) {
       // Load HTML from file or browser
       // Test extraction methods
   }
   ```

#### Duplicate Notifications Being Sent

**Cause:** Notification tracking file corrupted or deleted

**Solution:**
1. Delete `data/notifications_history.txt`
2. Bot will restart tracking
3. May send old notifications once (expected)

**Prevent:**
```bash
# Backup notification history
cp data/notifications_history.txt data/notifications_history.backup.txt
```

---

### 4. Telegram Integration Issues

#### Messages Not Arriving

**Step 1: Verify Bot Token**
```bash
export TELEGRAM_BOT_TOKEN=your_token_here
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/getMe"

# Should return bot info
```

**Step 2: Verify Chat ID**
```bash
# Send message to your chat ID
export CHAT_ID=your_chat_id_here
curl "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage?chat_id=${CHAT_ID}&text=Test"

# Should return success
```

**Step 3: Check Logs**
```bash
tail -f logs/taxila-bot.log | grep -i telegram
```

**Common Errors:**

| Error | Cause | Fix |
|-------|-------|-----|
| `bad request` | Invalid token | Verify token from @BotFather |
| `Forbidden: bot can't initiate conversation` | Wrong chat ID | Send message to bot first |
| `Connection refused` | Network issue | Check internet, firewall |
| `Unauthorized` | Token expired | Get new token from @BotFather |

#### Telegram Rate Limiting

**Error:** `429 Too Many Requests`

**Solution:**
1. Increase CHECK_INTERVAL_MINUTES
2. Don't send test messages frequently
3. Contact Telegram support if blocked

**Temporary Fix:**
```java
// Add delay in TelegramNotifier.java
Thread.sleep(1000); // Wait 1 second before sending
```

---

### 5. Memory and Performance Issues

#### High Memory Usage

**Check Memory:**
```bash
# During execution
jps -l -m  # See Java processes
top        # Monitor memory

# Or in Docker
docker stats taxila-notification-bot
```

**Reduce Memory Usage:**

1. **Reduce check interval:**
   ```bash
   export CHECK_INTERVAL_MINUTES=30
   ```

2. **Force garbage collection:**
   ```java
   // Add after notifications check
   System.gc();
   ```

3. **Close unused resources:**
   - Already closing driver between iterations
   - Already closing HTTP client

4. **Upgrade system:**
   - Render: Switch to paid tier
   - VPS: Increase RAM
   - Local: Close other apps

#### Slow Performance

**Optimization Tips:**

1. **Disable image loading** (already done)
2. **Use smaller check intervals for testing:**
   ```bash
   export CHECK_INTERVAL_MINUTES=2
   ```

3. **Cache page elements:**
   ```java
   // Store driver in class variable
   driver = null; // Reset between iterations
   ```

4. **Profile execution:**
   ```bash
   # Time each iteration
   tail -f logs/taxila-bot.log | grep "Iteration"
   ```

---

### 6. Docker Issues

#### Error: "Docker daemon not running"

**Solution:**
- Start Docker Desktop (Windows/Mac)
- Start Docker service (Linux): `sudo systemctl start docker`

#### Build Fails in Docker

**Solution:**
```bash
# Build with verbose output
docker-compose build --no-cache --progress=plain

# Check for errors
docker build --tag test . --progress=plain
```

#### Permission Denied Error

**Solution:**
```bash
# Add user to docker group (Linux)
sudo usermod -aG docker $USER
sudo newgrp docker
```

---

### 7. Cloud Deployment Issues

#### Render: Service Won't Start

**Check Logs:**
1. Go to Render Dashboard
2. Click your service
3. Click **Logs** tab
4. Look for error messages

**Common Issues:**

| Issue | Solution |
|-------|----------|
| Build timeout | Increase build timeout in settings |
| Out of memory | Upgrade plan or reduce check interval |
| Git auth | Reconnect GitHub |
| Missing env vars | Add all 4 required variables |

#### Render: Service Keeps Crashing

**Debug:**
1. View detailed logs in Render dashboard
2. Check if env vars are correct
3. Try locally first: `java -jar target/....jar`

**Solution:**
```bash
# Redeploy
cd your-repo
git push origin main

# Render auto-redeploys
```

#### Deployment: "Chrome not found"

**Cause:** Headless browser not installed in container

**Solution:**
- Already included in Dockerfile
- Ensure Docker image builds successfully
- Check `Dockerfile` has chromium installation

---

### 8. Network Issues

#### Connection Timeout

**Cause:** Network connectivity problem

**Solutions:**

1. **Check internet:**
   ```bash
   ping google.com
   ping taxila-aws.bits-pilani.ac.in
   ```

2. **Check firewall:**
   ```bash
   # Linux
   sudo ufw status
   
   # macOS
   sudo /usr/libexec/ApplicationFirewall/socketfilterfw --getglobalstate
   ```

3. **Check proxy settings:**
   - Some networks require proxy configuration
   - Add to TaxilaBot.java:
   ```java
   Proxy proxy = new Proxy(Proxy.Type.HTTP, 
       new InetSocketAddress("proxy.bits.ac.in", 8080));
   ```

#### SSL Certificate Errors

**Error:** `javax.net.ssl.SSLHandshakeException`

**Solution:**

1. **Verify certificate:**
   ```bash
   openssl s_client -connect taxila-aws.bits-pilani.ac.in:443
   ```

2. **Ignore for testing (not for production):**
   ```java
   // Add to TaxilaBot.java (temporary only)
   // This is insecure! Don't use in production
   ```

---

### 9. Logging Issues

#### Logs Not Appearing

**Check:**
1. Verify `logback.xml` is in `src/main/resources/`
2. Ensure it's included in JAR: `jar tf target/....jar | grep logback`
3. Check file permissions: `ls -la logs/`

**Solution:**
```bash
# Force console logging
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG \
     -jar target/taxila-notification-bot-1.0.0.jar
```

#### Log Files Growing Too Large

**Solution:** Automatic rotation is configured
- Rotates at 10MB
- Keeps 30 days or 1GB total
- Edit `logback.xml` to change limits

---

### 10. Debugging Tips

#### Enable Maximum Logging

1. Edit `src/main/resources/logback.xml`
2. Change level to DEBUG:
   ```xml
   <logger name="com.taxilabot" level="DEBUG" />
   <logger name="org.openqa.selenium" level="DEBUG" />
   ```

3. Recompile: `mvn clean package`

#### Create Debug Log File

```bash
# Capture all output
java -jar target/taxila-notification-bot-1.0.0.jar > debug.log 2>&1

# Monitor in real-time
tail -f debug.log
```

#### Save HTML for Analysis

**Add to TaxilaBot.java:**
```java
Files.write(Paths.get("debug_page.html"), 
    driver.getPageSource().getBytes());
```

#### Test Components Individually

```java
// Test Telegram
TelegramNotifier notifier = new TelegramNotifier(token, chatId);
notifier.testConnection();

// Test HTML parsing
Document doc = Jsoup.parse(htmlString);
List<NotificationData> notifications = extractNotifications(doc);

// Test login
TaxilaBot bot = new TaxilaBot(user, pass, token, chatId, 10);
bot.initializeDriver();
bot.loginToTaxila();
```

---

## Getting Help

**Resources:**
1. Check logs: `tail -f logs/taxila-bot.log`
2. Review full README.md
3. Check GitHub issues
4. Test components individually
5. Enable DEBUG logging

**Report Issues:**
- Include error messages
- Attach relevant logs
- Describe what you tried
- Include system info (OS, Java version)

---

**Last Updated:** March 2026
