# Project Structure & Architecture

## Complete Directory Layout

```
elern_bot/
├── src/
│   └── main/
│       ├── java/com/taxilabot/
│       │   ├── Main.java                          # Application entry point
│       │   │
│       │   ├── bot/
│       │   │   └── TaxilaBot.java                 # Core automation logic
│       │   │       - Selenium browser automation
│       │   │       - Login handling
│       │   │       - Notification extraction
│       │   │       - Main loop (24/7 operation)
│       │   │
│       │   ├── model/
│       │   │   └── NotificationData.java          # Notification data object
│       │   │       - Stores title, message, timestamp
│       │   │       - JSON serialization support
│       │   │       - Unique ID generation
│       │   │
│       │   ├── notify/
│       │   │   └── TelegramNotifier.java          # Telegram integration
│       │   │       - Sends messages via Telegram API
│       │   │       - Retry logic (3 attempts)
│       │   │       - Message formatting
│       │   │       - Connection testing
│       │   │
│       │   └── util/
│       │       ├── BotLogger.java                 # Centralized logging
│       │       │   - Timestamp formatting
│       │       │   - Log levels (DEBUG, INFO, WARN, ERROR)
│       │       │   - File & console logging
│       │       │
│       │       ├── ConfigManager.java             # Configuration management
│       │       │   - Environment variables
│       │       │   - Configuration validation
│       │       │   - Singleton pattern
│       │       │
│       │       └── NotificationTracker.java       # Duplicate prevention
│       │           - Tracks seen notifications
│       │           - File-based persistence
│       │           - Unique ID matching
│       │
│       └── resources/
│           └── logback.xml                        # Logging configuration
│               - Console & file output
│               - Log rotation (10MB)
│               - Retention policy
│
├── Configuration Files
│   ├── pom.xml                                    # Maven build configuration
│   │   - Dependencies (Selenium, JSoup, etc.)
│   │   - Plugins (shade, jar, compiler)
│   │   - Java version (11)
│   │
│   ├── Procfile                                   # Cloud deployment (Render)
│   │   - Specifies worker startup command
│   │
│   ├── Dockerfile                                 # Docker container config
│   │   - Multi-stage build
│   │   - Chrome installation
│   │   - Health checks
│   │
│   └── docker-compose.yml                        # Docker Compose setup
│       - Volume mounting
│       - Environment variables
│       - Resource limits
│       - Health checks
│
├── Documentation
│   ├── README.md                                  # Complete documentation
│   │   - Features & tech stack
│   │   - Setup instructions
│   │   - Configuration details
│   │   - Logging overview
│   │   - Performance optimization
│   │   - API references
│   │   - Security considerations
│   │
│   ├── QUICK_START.md                            # Quick setup guide
│   │   - 30-second setup
│   │   - Local testing
│   │   - Cloud deployment quick steps
│   │   - Common issues
│   │
│   ├── DEPLOYMENT.md                             # Detailed deployment guide
│   │   - Render setup (recommended)
│   │   - Cloud platforms (AWS, Azure, GCP, etc.)
│   │   - Self-hosted VPS setup
│   │   - Docker Compose
│   │   - Cost comparison
│   │   - Monitoring
│   │   - Scaling
│   │
│   ├── TROUBLESHOOTING.md                        # Problem-solving guide
│   │   - Common errors & solutions
│   │   - Build issues
│   │   - Login failures
│   │   - Notification problems
│   │   - Memory optimization
│   │   - Network issues
│   │   - Debugging tips
│   │
│   └── PROJECT_STRUCTURE.md                      # This file
│       - Architecture overview
│       - Component descriptions
│       - Data flow
│       - Class responsibilities
│
├── Setup Scripts
│   ├── setup.sh                                   # Linux/macOS setup
│   │   - Java/Maven installation
│   │   - Environment setup
│   │   - Automated build
│   │
│   └── setup.bat                                  # Windows setup
│       - Java/Maven verification
│       - Environment setup
│       - Automated build
│
├── Environment
│   └── .env.example                               # Template for credentials
│       - Username/password
│       - Telegram settings
│       - Optional parameters
│
├── .gitignore                                     # Git ignore rules
│   - .env file (credentials)
│   - Build artifacts
│   - Logs & data
│   - IDE files
│
├── target/ (Generated after build)
│   ├── classes/                                   # Compiled .class files
│   ├── taxila-notification-bot-1.0.0.jar        # Executable JAR
│   ├── taxila-notification-bot-1.0.0-sources.jar # Source code
│   └── maven-*/ (Build metadata)
│
└── Runtime Generated
    ├── .env                                       # Your credentials (DO NOT COMMIT)
    │
    ├── logs/
    │   ├── taxila-bot.log                        # Current log file
    │   └── taxila-bot.YYYY-MM-DD.*.log          # Rotated logs
    │
    └── data/
        └── notifications_history.txt             # Seen notification IDs
```

## Architecture Overview

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       TAXILA BOT                             │
│                                                              │
│  ┌──────────────┐                                           │
│  │    Main      │ Entry Point                               │
│  └──────┬───────┘                                           │
│         │                                                   │
│         ▼                                                   │
│  ┌──────────────────┐                                      │
│  │ ConfigManager    │ ◄─── .env / Environment Variables    │
│  └──────┬───────────┘                                      │
│         │                                                  │
│         ▼                                                  │
│  ┌─────────────────────────────────────────┐              │
│  │      TaxilaBot (Main Loop - 24/7)       │              │
│  │                                         │              │
│  │  1. Initialize WebDriver                │              │
│  │  2. Login to Taxila                     │              │
│  │  3. Navigate to Notifications           │              │
│  │  4. Extract HTML via JSoup              │              │
│  │  5. Parse Notifications                 │              │
│  │  6. Check for Duplicates                │              │
│  │  7. Send to Telegram (if new)           │              │
│  │  8. Wait 10 minutes                     │              │
│  │  9. Repeat from step 1                  │              │
│  │                                         │              │
│  └──────┬──────────────────────────────────┘              │
│         │ Uses                                            │
│         │                                                │
│  ┌──────┴────────────────┬──────────────────┐           │
│  │                       │                  │           │
│  ▼                       ▼                  ▼           │
│ WebDriver          NotificationTracker   TelegramNotifier │
│ (Selenium)         (Duplicate Check)     (API Calls)     │
│                                                          │
│ • Login              • File I/O           • HTTP         │
│ • Navigation         • Unique IDs         • Retry Logic  │
│ • HTML Parse         • Memory Track       • Formatting   │
│                                                          │
└──────────────────────────────────────────────────────────────┘
```

### Data Flow

```
START
  │
  ▼
┌─────────────────────┐
│ Load Configuration  │  (from .env or env vars)
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ Test Telegram       │  (connectivity check)
└────────┬────────────┘
         │
    ┌────▼──────────────────────────────────────┐
    │    LOOP: Every CHECK_INTERVAL_MINUTES     │
    │                                           │
    │  1. Initialize Chrome Driver              │
    │  2. Connect to Taxila Portal              │
    │  3. Enter Username & Password             │
    │  4. Wait for Login                        │
    │  5. Navigate to Notifications             │
    │  6. Extract Page HTML                     │
    │  7. Parse with JSoup                      │
    │  8. Get Notification List                 │
    │                                           │
    │  For Each Notification:                   │
    │  ├─ Check if Seen Before                  │
    │  │  (in notifications_history.txt)        │
    │  │                                        │
    │  └─ If NEW:                               │
    │     ├─ Format Message                     │
    │     ├─ Send via Telegram API              │
    │     └─ Append to History File             │
    │                                           │
    │  9. Close Browser                         │
    │  10. Wait CHECK_INTERVAL_MINUTES          │
    │  11. Go to Step 1                         │
    │                                           │
    └─────────────────────────────────────────┘
```

## Class Responsibilities

### Main.java
**Purpose:** Application entry point  
**Responsibilities:**
- Load configuration
- Validate environment
- Initialize bot
- Start main loop
- Handle shutdown

```java
public static void main(String[] args)
  → Creates TaxilaBot instance
  → Calls bot.start() (infinite loop)
```

### TaxilaBot.java
**Purpose:** Core automation logic  
**Key Methods:**
- `start()` - Main infinite loop (24/7 operation)
- `checkNotifications()` - Check for new notifications
- `initializeDriver()` - Setup Selenium WebDriver
- `loginToTaxila()` - Handle portal login
- `navigateToNotifications()` - Go to notifications page
- `extractNotifications()` - Parse HTML for notifications

**Lifecycle:**
1. Initializes WebDriver (Chrome)
2. Logs in with credentials
3. Navigates to notifications
4. Extracts from HTML
5. Returns list of NotificationData
6. Closes driver for cleanup

### ConfigManager.java
**Purpose:** Configuration management  
**Features:**
- Singleton pattern (single instance)
- Reads environment variables
- Validates required configs
- Provides default values

**Configuration Keys:**
```
TAXILA_USERNAME        (required)
TAXILA_PASSWORD        (required)
TELEGRAM_BOT_TOKEN     (required)
TELEGRAM_CHAT_ID       (required)
CHECK_INTERVAL_MINUTES (optional, default: 10)
HEADLESS_MODE         (optional, default: true)
DATA_DIRECTORY        (optional, default: ./data)
```

### NotificationData.java
**Purpose:** Notification object model  
**Fields:**
- `title` - Notification title
- `message` - Notification content
- `timestamp` - When notification appeared
- `source` - Always "BITS_TAXILA"
- `uniqueId` - Hash-based unique identifier

**Features:**
- JSON serialization/deserialization
- Unique ID generation
- Equality checking

### NotificationTracker.java
**Purpose:** Prevent duplicate notifications  
**Features:**
- File-based persistence
- In-memory cache
- Efficient lookup

**File:** `data/notifications_history.txt`
- One ID per line
- Human-readable hex format

### TelegramNotifier.java
**Purpose:** Telegram message delivery  
**Features:**
- HTTP GET requests to Telegram API
- Retry logic (3 attempts)
- Connection testing
- HTML message formatting

**API Endpoint:**
```
https://api.telegram.org/bot{TOKEN}/sendMessage
Parameters: chat_id, text, parse_mode
```

### BotLogger.java
**Purpose:** Centralized logging  
**Log Levels:**
- DEBUG - Detailed information
- INFO - General messages
- WARN - Warnings
- ERROR - Errors with stack trace

**Output:**
- Console (real-time)
- File (`logs/taxila-bot.log`)

## Package Structure

### com.taxilabot
- **Main Application Entry**

### com.taxilabot.bot
- **Automation Logic**
- TaxilaBot (browser automation, login, scraping)

### com.taxilabot.model
- **Data Objects**
- NotificationData (notification representation)

### com.taxilabot.notify
- **External Communication**
- TelegramNotifier (Telegram API integration)

### com.taxilabot.util
- **Utility Classes**
- BotLogger (logging)
- ConfigManager (configuration)
- NotificationTracker (duplicate prevention)

## Dependencies & Libraries

### Core Framework
- **Selenium 4.15** - WebDriver & browser automation
- **WebDriverManager 5.6** - Automatic ChromeDriver handling
- **JSoup 1.16** - HTML parsing

### Communication
- **Apache HttpClient 4.5** - HTTP requests
- **Gson 2.10** - JSON parsing

### Infrastructure
- **SLF4J 2.0** - Logging API
- **Logback 1.4** - Logging implementation

### Build Tools
- **Maven 3.8+** - Build automation
- **Java 11+** - Runtime

## Build Process

```
pom.xml
  ├── Download Dependencies
  ├── Compile Java Sources
  ├── Run Tests
  ├── Create JAR
  ├── Create Shade JAR (fat JAR with all dependencies)
  └── Output: target/taxila-notification-bot-1.0.0.jar
```

## Running the Application

### Execution Flow

```
1. Start                          2. Configure
   os: java -jar ...                 read: .env
                                     validate: all keys

3. Initialize                     4. Test Connection
   setup: WebDriver                  check: Telegram API
   load: configs

5. Main Loop (infinite)
   ├─ Initialize WebDriver
   ├─ Login to Taxila
   ├─ Navigate to Notifications
   ├─ Extract HTML
   ├─ Parse Notifications
   ├─ Check for Duplicates
   ├─ Send New to Telegram
   ├─ Close WebDriver
   ├─ Sleep 10 minutes
   └─ Loop back to ├─ Initialize

6. Exit (on error or signal)
   ├─ Close WebDriver
   ├─ Close HTTP client
   └─ Log shutdown
```

## File I/O

### Input Files
- `.env` - Environment variables
- `src/main/resources/logback.xml` - Logging config

### Output Files (Generated at Runtime)
- `logs/taxila-bot.log` - Current log
- `logs/taxila-bot.YYYY-MM-DD.*.log` - Rotated logs
- `data/notifications_history.txt` - Seen notification IDs

## Performance Characteristics

### Memory Usage
- Idle: ~100-150 MB
- During check: ~200-300 MB
- Peak: ~400 MB (with browser)

### CPU Usage
- Idle: <1%
- During check: 10-30%
- Browser automation: 20-50%

### Network Usage
- Per check: ~1-2 MB
- Telegram message: ~1-5 KB
- Login: ~200-500 KB

## Security Model

### Credential Storage
- Credentials stored in `.env` (local only)
- Never logged
- Not included in source code

### Communication
- HTTPS for Telegram API
- HTTPS for Taxila portal
- SSL certificate validation enabled

### Data Retention
- Notification history: File-based (local)
- No remote storage
- Persists across restarts

## Extension Points

### Adding New Notification Channels
Extend `com.taxilabot.notify`:
```
NotificationChannel interface
├─ TelegramNotifier (existing)
├─ EmailNotifier (example)
├─ SlackNotifier (example)
└─ WebhookNotifier (example)
```

### Custom Notification Parsing
Edit `extractNotifications()` in TaxilaBot:
```
Modify CSS selectors
Adjust parsing logic
Test with new HTML structure
```

### Additional Configuration
Edit `ConfigManager.java`:
```
Add new environment variable
Update validation logic
Provide getter method
```

---

**Last Updated:** March 2026  
**Version:** 1.0.0  
**Status:** Production Ready
