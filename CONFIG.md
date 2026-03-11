# Environment Configuration Guide

This document explains how to securely configure the Taxila Notification Bot using environment variables.

## ⚠️ Security First

**NEVER commit `.env` file to version control.** It's already in `.gitignore` and excluded from the repository.

## Setup Instructions

### Windows (PowerShell)
```powershell
# Run the setup script
.\setup.bat

# Or manually create .env from template
Copy-Item .env.example .env
# Then edit .env with your credentials
```

### Linux/macOS (Bash)
```bash
# Run the setup script
bash setup.sh

# Or manually create .env from template
cp .env.example .env
# Then edit .env with your credentials
```

## Required Configuration

These variables **MUST** be set, or the bot will not start:

### `TAXILA_USERNAME`
- Your BITS WILP email address
- Format: `xxxxxxx@wilp.bits-pilani.ac.in`
- Used to log into the Taxila portal

### `TAXILA_PASSWORD`
- Your Taxila portal password
- Keep this secure and never commit it
- Consider using a strong, unique password

### `TELEGRAM_BOT_TOKEN`
- Token from BotFather on Telegram
- Format: `123456789:ABCdefGHIjklmnoPQRstuvwxyz...`
- Create via: https://t.me/botfather

### `TELEGRAM_CHAT_ID`
- Telegram chat ID where notifications are sent
- Numeric value (e.g., `1036633702`)
- Get it from: https://t.me/userinfobot

## Optional Configuration

These variables have sensible defaults if not set:

### `CHECK_INTERVAL_MINUTES` (default: `10`)
- How often to check for new notifications (in minutes)
- Minimum recommended: 5 minutes

### `HEADLESS_MODE` (default: `true`)
- Whether to hide the browser window
- Set to `false` for debugging

### `DATA_DIRECTORY` (default: `./data`)
- Where to store notification history
- Ensure the directory is writable

## Setting Environment Variables

### Option 1: Via `.env` file (Recommended for local development)
```
# .env
TAXILA_USERNAME=your_email@wilp.bits-pilani.ac.in
TAXILA_PASSWORD=your_password
TELEGRAM_BOT_TOKEN=your_token
TELEGRAM_CHAT_ID=your_chat_id
CHECK_INTERVAL_MINUTES=10
```

### Option 2: Via PowerShell (for testing)
```powershell
$env:TAXILA_USERNAME = "your_email@wilp.bits-pilani.ac.in"
$env:TAXILA_PASSWORD = "your_password"
$env:TELEGRAM_BOT_TOKEN = "your_token"
$env:TELEGRAM_CHAT_ID = "your_chat_id"
```

### Option 3: Via Command Prompt (for testing)
```batch
set TAXILA_USERNAME=your_email@wilp.bits-pilani.ac.in
set TAXILA_PASSWORD=your_password
set TELEGRAM_BOT_TOKEN=your_token
set TELEGRAM_CHAT_ID=your_chat_id
```

### Option 4: Via Bash (for testing)
```bash
export TAXILA_USERNAME="your_email@wilp.bits-pilani.ac.in"
export TAXILA_PASSWORD="your_password"
export TELEGRAM_BOT_TOKEN="your_token"
export TELEGRAM_CHAT_ID="your_chat_id"
```

## Getting Your Telegram Credentials

1. **Telegram Bot Token**:
   - Open Telegram and chat with [@BotFather](https://t.me/botfather)
   - Send `/newbot` and follow instructions
   - Save the token provided

2. **Chat ID**:
   - Chat with [@userinfobot](https://t.me/userinfobot) or [@getidsbot](https://t.me/getidsbot)
   - The bot will reply with your numeric Chat ID

## Docker Deployment

For Docker, set environment variables during container startup:

```bash
docker run -e TAXILA_USERNAME="..." \
           -e TAXILA_PASSWORD="..." \
           -e TELEGRAM_BOT_TOKEN="..." \
           -e TELEGRAM_CHAT_ID="..." \
           taxila-notification-bot
```

Or use an environment file:
```bash
docker run --env-file .env taxila-notification-bot
```

## Troubleshooting

**Bot won't start - "Configuration invalid"**
- Verify all required environment variables are set
- Check for typos in variable names (case-sensitive)
- Ensure values don't have leading/trailing spaces

**Bot connects but doesn't receive notifications**
- Verify Telegram bot token is correct
- Check Chat ID is correct (use [@userinfobot](https://t.me/userinfobot))
- Ensure bot is admin in the target Chat/Group

**Permission denied running setup.sh**
```bash
chmod +x setup.sh
bash setup.sh
```

## Configuration Validation

The bot automatically validates configuration on startup:
- Missing required variables → Bot exits with error
- Invalid format → Bot logs warning and uses defaults
- Valid configuration → Bot starts normally

Check [ConfigManager.java](src/main/java/com/taxilabot/util/ConfigManager.java) for implementation details.
