# Cloud Deployment Guide

## Render.com Deployment (Recommended)

Render is perfect for background workers. Bot runs 24/7 free tier available.

### Prerequisites
- GitHub account with repository pushed
- Render account (free)
- Telegram bot token
- BITS Taxila credentials

### Step-by-Step Deployment

#### 1. Prepare GitHub Repository

```bash
# Create .gitignore to exclude sensitive files
cat > .gitignore << EOF
.env
data/
logs/
target/
*.class
.DS_Store
.idea/
*.iml
node_modules/
EOF

# Add and push
git add .
git commit -m "Add Taxila Notification Bot"
git push origin main
```

#### 2. Create Render Account

1. Visit https://render.com
2. Sign up with GitHub (click "Connect with GitHub")
3. Authorize Render to access your repositories

#### 3. Create Background Worker

1. In Render Dashboard, click **New +**
2. Select **Background Worker**
3. Select your repository
4. Fill in configuration:

```
Service Name: taxila-notification-bot
Runtime: Docker
Build Command: mvn clean package
Start Command: java -jar target/taxila-notification-bot-1.0.0.jar
```

5. Click **Create Background Worker**

#### 4. Configure Environment Variables

After service is created:

1. Go to **Environment** tab
2. Add each variable:

| Key | Value | Example |
|-----|-------|---------|
| TAXILA_USERNAME | Your BITS Roll Number | 2022A1PS0123G |
| TAXILA_PASSWORD | Your Taxila Password | MyPassword123 |
| TELEGRAM_BOT_TOKEN | From @BotFather | 123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11 |
| TELEGRAM_CHAT_ID | Your Chat ID | 987654321 |
| CHECK_INTERVAL_MINUTES | Frequency (optional) | 10 |

3. Click **Save**

#### 5. Deploy

1. Go to **Deploys** tab
2. Click **Deploy Latest Commit**
3. Watch logs in real-time to confirm startup

Your bot is now running 24/7! ✅

### Monitor Your Bot

**View Logs:**
1. Click your service in dashboard
2. Go to **Logs** tab
3. See real-time output

**Troubleshoot:**
- Look for ERROR lines in logs
- Check environment variables are set
- Verify TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID are correct

**Stop/Restart:**
1. In service page, click **Settings**
2. Click **Suspend** to stop
3. Click **Resume** to start again

**View Resource Usage:**
1. Click your service
2. Go to **Metrics** tab
3. Monitor CPU, Memory, Disk usage

### Costs

**Free Tier:**
- 750 hours/month compute time (enough for 24/7)
- 0.1 GB memory
- Shared CPU

**Paid Tier:**
- $7/month for dedicated resources
- Better performance on high-traffic portals
- Priority support

---

## AWS Lambda Deployment (Advanced)

Not recommended for this use case (Lambda has execution time limits).

---

## Google Cloud Run Deployment

### Prerequisites
- Google Cloud account
- Docker installed locally
- gcloud CLI installed

### Create Dockerfile

```dockerfile
FROM maven:3.8-openjdk-11-slim as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/taxila-notification-bot-1.0.0.jar .
COPY --from=builder /app/src/main/resources/logback.xml .
ENV PORT=8080
CMD ["java", "-jar", "taxila-notification-bot-1.0.0.jar"]
```

### Deploy Steps

1. **Create Google Cloud Project:**
   ```bash
   gcloud projects create taxila-bot
   gcloud config set project taxila-bot
   ```

2. **Enable Cloud Run API:**
   ```bash
   gcloud services enable cloudrun.googleapis.com
   ```

3. **Build and Push Container:**
   ```bash
   gcloud builds submit --tag gcr.io/taxila-bot/taxila-notification-bot
   ```

4. **Deploy to Cloud Run:**
   ```bash
   gcloud run deploy taxila-notification-bot \
     --image gcr.io/taxila-bot/taxila-notification-bot \
     --platform managed \
     --memory 512Mi \
     --timeout 3600 \
     --set-env-vars TAXILA_USERNAME=your_username,TAXILA_PASSWORD=your_password,TELEGRAM_BOT_TOKEN=your_token,TELEGRAM_CHAT_ID=your_chat_id
   ```

---

## Azure Container Instances

### Create deployment.yaml

```yaml
apiVersion: '2021-07-01'
name: taxila-notification-bot
properties:
  containers:
  - name: taxila-bot
    properties:
      image: taxila-notification-bot:latest
      resources:
        requests:
          cpu: 0.5
          memoryInGb: 0.5
      environmentVariables:
      - name: TAXILA_USERNAME
        value: your_username
      - name: TAXILA_PASSWORD
        secureValue: your_password
      - name: TELEGRAM_BOT_TOKEN
        secureValue: your_token
      - name: TELEGRAM_CHAT_ID
        value: your_chat_id
  osType: Linux
  restartPolicy: Always
```

### Deploy

```bash
az container create --resource-group myResourceGroup --file deployment.yaml
```

---

## DigitalOcean App Platform

### 1. Connect GitHub Repository

1. Visit https://cloud.digitalocean.com
2. Go to App Platform
3. Click **Create App**
4. Select your GitHub repository

### 2. Configure Build

- Source: GitHub
- Repository: your-repo
- Branch: main
- Dockerfile Path: Dockerfile

### 3. Set Environment Variables

Add in App Platform settings:
- TAXILA_USERNAME
- TAXILA_PASSWORD
- TELEGRAM_BOT_TOKEN
- TELEGRAM_CHAT_ID

### 4. Deploy

Click **Deploy** button. Runs automatically.

---

## Self-Hosted Deployment (VPS)

### Setup on Ubuntu/Debian VPS

1. **Install Java and Maven:**
   ```bash
   sudo apt update
   sudo apt install openjdk-11-jdk-headless maven git -y
   ```

2. **Install Chrome:**
   ```bash
   sudo apt install chromium-browser -y
   ```

3. **Clone Repository:**
   ```bash
   git clone https://github.com/yourname/elern_bot.git
   cd elern_bot
   ```

4. **Setup Environment:**
   ```bash
   cp .env.example .env
   nano .env  # Edit with your credentials
   ```

5. **Build:**
   ```bash
   mvn clean package
   ```

6. **Create systemd Service:**
   ```bash
   sudo nano /etc/systemd/system/taxila-bot.service
   ```

   Add:
   ```ini
   [Unit]
   Description=Taxila Notification Bot
   After=network.target

   [Service]
   Type=simple
   User=taxila
   WorkingDirectory=/home/taxila/elern_bot
   EnvironmentFile=/home/taxila/elern_bot/.env
   ExecStart=/usr/bin/java -jar /home/taxila/elern_bot/target/taxila-notification-bot-1.0.0.jar
   Restart=always
   RestartSec=10
   StandardOutput=journal
   StandardError=journal

   [Install]
   WantedBy=multi-user.target
   ```

7. **Enable and Start:**
   ```bash
   sudo systemctl enable taxila-bot
   sudo systemctl start taxila-bot
   sudo systemctl status taxila-bot
   ```

8. **Monitor:**
   ```bash
   sudo journalctl -u taxila-bot.service -f
   ```

---

## Docker Compose (Local/VPS)

### Create docker-compose.yml

```yaml
version: '3.8'
services:
  taxila-bot:
    build: .
    container_name: taxila-notification-bot
    restart: always
    environment:
      TAXILA_USERNAME: ${TAXILA_USERNAME}
      TAXILA_PASSWORD: ${TAXILA_PASSWORD}
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
      TELEGRAM_CHAT_ID: ${TELEGRAM_CHAT_ID}
      CHECK_INTERVAL_MINUTES: 10
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### Run

```bash
docker-compose up --build -d
docker-compose logs -f
```

---

## Cost Comparison

| Platform | Free Tier | Price | Uptime | Best For |
|----------|-----------|-------|--------|----------|
| Render | 750 hrs/mo | $7/mo | 99.9% | Easy setup |
| Railway | $5 credit | $0.14/GB | 99.9% | Budget-friendly |
| Heroku | None | $7/mo+ | 99.95% | Production |
| AWS Lambda | 1M requests | $0.00001667/s | Varies | Event-driven |
| Google Cloud | $300 credit | $0.29/GB | 99.95% | Google services |
| Azure | $200 credit | Competitive | 99.95% | Enterprise |
| DigitalOcean | None | $6/mo | 99.99% | Best support |
| Self-Hosted | None | $5-20/mo | Your uptime | Full control |

---

## Monitoring and Alerts

### Enable Monitoring on Render

1. Go to Service Settings
2. Under **Notifications**, enable:
   - Deploy failed
   - Service suspended
   - Resource limits exceeded

### Setup Custom Monitoring

Create `monitor.sh`:
```bash
#!/bin/bash
curl -s https://api.render.com/v1/services/<SERVICE_ID> \
  --header "Authorization: Bearer $RENDER_API_KEY" | \
  jq '.service.state'
```

---

## Troubleshooting Deployment

### Service Won't Start

1. Check logs: `docker logs container_id`
2. Verify env vars are set
3. Ensure JAR built successfully

### High Memory Usage

1. Reduce CHECK_INTERVAL_MINUTES
2. Close driver between checks
3. Upgrade to paid tier

### Login Failures

1. Verify credentials work manually
2. Check if Taxila portal is accessible from cloud
3. Add delay between login attempts

### Telegram Not Delivering

1. Verify bot token with: `curl https://api.telegram.org/bot<TOKEN>/getMe`
2. Verify chat ID in logs
3. Check Telegram isn't blocking your IP

---

## Scaling

For multiple portals/users:
1. Deploy separate instances
2. Use different bot tokens per instance
3. Monitor resource usage
4. Upgrade plan if needed

---

Last Updated: March 2026
