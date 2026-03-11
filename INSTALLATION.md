# Installation & Run Instructions

Complete step-by-step guide to get the bot running.

## Prerequisites

### Minimum Requirements
- Java 11 or higher
- Maven 3.6 or higher
- Git
- For cloud: GitHub account, Render account

### Optional
- Docker & Docker Compose (for container deployment)
- Terminal/Command Prompt knowledge

## System-Specific Installation

### Windows Setup

#### 1. Install Java 11
1. Visit [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
2. Download Java 11 (LTS)
3. Run installer and follow instructions
4. Verify installation:
   ```cmd
   java -version
   ```

#### 2. Install Maven
1. Visit [Apache Maven Download](https://maven.apache.org/download.cgi)
2. Download Apache Maven 3.9.5 (or latest)
3. Extract to a folder (e.g., `C:\tools\maven`)
4. Add to PATH:
   - Right-click This PC → Properties
   - Advanced System Settings
   - Environment Variables
   - Add `C:\tools\maven\bin` to PATH
5. Verify installation:
   ```cmd
   mvn -version
   ```

#### 3. Clone Repository
```cmd
cd C:\Users\YourUsername\Desktop
git clone https://github.com/yourname/elern_bot.git
cd elern_bot
```

#### 4. Run Setup Script
```cmd
setup.bat
```

This will:
- Verify Java & Maven
- Create `.env` file
- Ask for credentials
- Build the project

#### 5. Run the Bot
```cmd
java -jar target\taxila-notification-bot-1.0.0.jar
```

---

### Linux/Ubuntu Setup

#### 1. Install Java 11
```bash
sudo apt update
sudo apt install openjdk-11-jdk-headless -y
java -version
```

#### 2. Install Maven
```bash
sudo apt install maven -y
mvn -version
```

#### 3. Install Chrome
```bash
sudo apt install chromium-browser -y
chromium-browser --version
```

#### 4. Clone Repository
```bash
cd ~
git clone https://github.com/yourname/elern_bot.git
cd elern_bot
```

#### 5. Run Setup Script
```bash
chmod +x setup.sh
./setup.sh
```

#### 6. Run the Bot
```bash
java -jar target/taxila-notification-bot-1.0.0.jar
```

---

### macOS Setup

#### 1. Install Java 11
```bash
# Using Homebrew
brew tap adoptopenjdk/openjdk
brew install openjdk@11
java -version
```

Or download from [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)

#### 2. Install Maven
```bash
brew install maven
mvn -version
```

#### 3. Install Chrome
```bash
brew install chromium
```

#### 4. Clone Repository
```bash
cd ~/Desktop
git clone https://github.com/yourname/elern_bot.git
cd elern_bot
```

#### 5. Run Setup Script
```bash
chmod +x setup.sh
./setup.sh
```

#### 6. Run the Bot
```bash
java -jar target/taxila-notification-bot-1.0.0.jar
```

---

## Manual Installation (All Systems)

### Step 1: Prerequisites

```bash
# Verify Java
java -version
# Must be 11 or higher

# Verify Maven
mvn -version
# Must be 3.6 or higher

# Verify Git
git --version
```

### Step 2: Clone Repository

```bash
git clone https://github.com/yourname/elern_bot.git
cd elern_bot
```

### Step 3: Create Environment File

```bash
cp .env.example .env
```

Edit `.env` file with your credentials:
```
TAXILA_USERNAME=2022A1PS0123G
TAXILA_PASSWORD=YourPassword123
TELEGRAM_BOT_TOKEN=123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11
TELEGRAM_CHAT_ID=987654321
```

### Step 4: Build Project

```bash
mvn clean package
```

This downloads dependencies and creates the executable JAR.

Output: `target/taxila-notification-bot-1.0.0.jar`

### Step 5: Run the Bot

#### Option A: Using JAR
```bash
java -jar target/taxila-notification-bot-1.0.0.jar
```

#### Option B: Using Maven
```bash
mvn clean compile exec:java -Dexec.mainClass="com.taxilabot.Main"
```

#### Option C: With Custom Environment File
```bash
source .env  # On Linux/macOS
# Or in PowerShell on Windows: Get-Content .env | ForEach-Object { if ($_) { $name, $value = $_.split('='); [Environment]::SetEnvironmentVariable($name, $value) } }

java -jar target/taxila-notification-bot-1.0.0.jar
```

### Step 6: Verify Installation

You should see output like:
```
[2024-03-11 10:30:45] [Main] ==================================================
[2024-03-11 10:30:45] [Main] Starting Taxila Notification Bot
[2024-03-11 10:30:45] [Main] ==================================================
[2024-03-11 10:30:46] [ConfigManager] Loaded configuration successfully
[2024-03-11 10:30:47] [TaxilaBot] Testing Telegram connection...
[2024-03-11 10:30:48] [TelegramNotifier] Message sent successfully to Telegram
[2024-03-11 10:30:48] [Main] Telegram connection successful!
[2024-03-11 10:30:49] [Main] ==================================================
[2024-03-11 10:30:49] [TaxilaBot] Iteration #1 - Starting notification check
```

---

## Docker Installation

### Using Docker Compose (Recommended)

```bash
# Build and start
docker-compose up --build -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Using Docker Directly

```bash
# Build image
docker build -t taxila-notification-bot .

# Run container
docker run -d \
  --name taxila-bot \
  -e TAXILA_USERNAME=your_username \
  -e TAXILA_PASSWORD=your_password \
  -e TELEGRAM_BOT_TOKEN=your_token \
  -e TELEGRAM_CHAT_ID=your_chat_id \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  taxila-notification-bot

# View logs
docker logs -f taxila-bot

# Stop
docker stop taxila-bot
```

---

## Telegram Bot Setup (If Not Done Yet)

### Get Telegram Bot Token

1. **Open Telegram** and search for `@BotFather`
2. **Start chat** and send `/start`
3. **Create bot** by sending `/newbot`
4. **Follow prompts:**
   - Give bot a name (e.g., "Taxila Notification Bot")
   - Give bot username (must end with "bot", e.g., "taxila_alert_bot")
5. **Copy bot token** from BotFather's response
   - Format: `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`

### Get Telegram Chat ID

1. **Message your bot** (send any message)
2. **Get Chat ID:**
   ```bash
   curl "https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates"
   ```
   Replace `<YOUR_TOKEN>` with your bot token
3. **Find Chat ID** in response:
   ```json
   {
     "ok": true,
     "result": [
       {
         "update_id": 123456789,
         "message": {
           "message_id": 1,
           "from": {
             "id": 987654321,  # <-- THIS IS YOUR CHAT ID
             ...
           }
         }
       }
     ]
   }
   ```

---

## Running for the First Time

### What to Expect

1. **Bot starts** - Loading configuration
2. **Tests Telegram** - Sends test message to verify connection works
3. **Initializes browser** - Downloads ChromeDriver automatically
4. **Logs in** - Enters your credentials into Taxila portal
5. **Checks notifications** - Extracts notifications from portal
6. **Sends alerts** - For any NEW notification found
7. **Waits** - Sleeps for CHECK_INTERVAL_MINUTES (default 10)
8. **Repeats** - Infinite loop continues 24/7

### Logs

Real-time logs appear in terminal, also saved to `logs/taxila-bot.log`

```bash
# Watch logs
tail -f logs/taxila-bot.log

# View specific errors
grep ERROR logs/taxila-bot.log

# View last 50 lines
tail -50 logs/taxila-bot.log
```

---

## Verification Checklist

✅ Java 11+ installed  
✅ Maven 3.6+ installed  
✅ Repository cloned  
✅ `.env` file created with credentials  
✅ Telegram Bot Token obtained  
✅ Telegram Chat ID obtained  
✅ Credentials verified manually on Taxila portal  
✅ Project built successfully (`mvn clean package`)  
✅ Bot started (`java -jar ...`)  
✅ Telegram test message received  
✅ Logs showing normal operation  

---

## Troubleshooting

### Build Fails
```bash
# Clear cache and rebuild
rm -rf ~/.m2/repository
mvn clean package -U
```

### Java Not Found
```bash
# Check path
echo $JAVA_HOME
java -version

# Install Java 11 if needed
```

### Maven Not Found
```bash
# Check Maven installation
mvn -version

# Add to PATH if needed
```

### Credentials Not Working
1. Test login manually: https://taxila-aws.bits-pilani.ac.in/login/index.php
2. Ensure no special characters need escaping
3. Check CAPS in username (Roll number format)
4. Verify password is correct

### No Telegram Messages
1. Verify token: `curl https://api.telegram.org/bot<TOKEN>/getMe`
2. Verify chat ID: `curl https://api.telegram.org/bot<TOKEN>/getUpdates`
3. Check logs for Telegram errors

For more help, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

---

## Next Steps

After successful installation:

1. **Local Testing**
   - Monitor logs
   - Test notification sending
   - Verify periodical checks

2. **Cloud Deployment**
   - See [DEPLOYMENT.md](DEPLOYMENT.md) for cloud setup
   - Push to GitHub
   - Deploy on Render/AWS/Azure/GCP

3. **Monitoring**
   - Set up log rotation
   - Monitor resource usage
   - Check notifications regular

4. **Customization**
   - Adjust CHECK_INTERVAL_MINUTES
   - Modify notification parsing
   - Add additional notification channels

5. **Production Setup**
   - Use systemd service (Linux)
   - Enable auto-restart
   - Setup monitoring alerts

---

**Last Updated:** March 2026  
**Version:** 1.0.0  

For detailed information, refer to [README.md](README.md) and [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
