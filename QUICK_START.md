# Quick Start Guide

## 30-Second Setup

### 1. Get Telegram Bot Token
1. Message @BotFather on Telegram → `/newbot` → follow steps
2. Message your new bot once
3. Visit `https://api.telegram.org/bot<TOKEN>/getUpdates`
4. Find your **Chat ID** in the response

### 2. Set Environment Variables
```bash
export TAXILA_USERNAME=your_roll_number
export TAXILA_PASSWORD=your_password
export TELEGRAM_BOT_TOKEN=your_token_from_botfather
export TELEGRAM_CHAT_ID=your_chat_id
```

### 3. Build & Run
```bash
mvn clean package
java -jar target/taxila-notification-bot-1.0.0.jar
```

## Local Testing

```bash
# Build
mvn clean package

# Run with default settings (10 min check interval)
java -jar target/taxila-notification-bot-1.0.0.jar

# Run with custom interval (5 minutes)
export CHECK_INTERVAL_MINUTES=5
java -jar target/taxila-notification-bot-1.0.0.jar
```

## Deploy to Render (Cloud)

### 1. Push to GitHub
```bash
git add .
git commit -m "Deploy Taxila Bot"
git push origin main
```

### 2. Create on Render
- Visit https://render.com
- Click **New** → **Background Worker**
- Select your GitHub repo
- Set **Build Command:** `mvn clean package`
- Set **Start Command:** `java -jar target/taxila-notification-bot-1.0.0.jar`

### 3. Add Environment Variables
In Render Dashboard → Your Service → Environment:
```
TAXILA_USERNAME=your_roll_number
TAXILA_PASSWORD=your_password
TELEGRAM_BOT_TOKEN=your_token
TELEGRAM_CHAT_ID=your_chat_id
```

### 4. Deploy
Click **Deploy** button. Bot runs forever!

## Check if Working

### Watch Logs
```bash
tail -f logs/taxila-bot.log
```

### Monitor Render Logs
- Go to Render Dashboard
- Click your service
- Watch **Logs** tab in real-time

### Test Telegram
Send a test notification from any bot to verify chat ID is working

## Stop the Bot

### Local
Press `Ctrl+C`

### Render
Click **Suspend** in Render Dashboard

## Common Issues

| Problem | Solution |
|---------|----------|
| Nothing happening | Check logs: `tail -f logs/taxila-bot.log` |
| Login failing | Verify username/password manually |
| No Telegram messages | Check bot token and chat ID with curl |
| Build error | Ensure Java 11+ and Maven 3.6+ installed |

## Next Steps

- Read full [README.md](README.md) for detailed docs
- Check logs for any errors
- Adjust `CHECK_INTERVAL_MINUTES` as needed
- Add to cron job for automatic restart (if local)

---

For detailed documentation, see [README.md](README.md)
