# 🔧 Jenkins Setup Guide for Swag Labs Tests

Complete step-by-step guide to set up Jenkins locally with Docker and configure it for your test automation project.

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Manual Jenkins Setup](#manual-jenkins-setup)
4. [GitHub Repository Setup](#github-repository-setup)
5. [Create Jenkins Pipeline Job](#create-jenkins-pipeline-job)
6. [Configure GitHub Webhooks](#configure-github-webhooks)
7. [Ngrok Setup (for local machines)](#ngrok-setup-for-local-machines)
8. [Install Jenkins Plugins](#install-jenkins-plugins)
9. [Test Your Pipeline](#test-your-pipeline)
10. [Advanced Configuration](#advanced-configuration)

---

## 📦 Prerequisites

Before starting, ensure you have:

- ✅ **Docker Desktop** installed ([download here](https://www.docker.com/products/docker-desktop))
- ✅ **Docker Compose** (included with Docker Desktop)
- ✅ **Git** installed ([download here](https://git-scm.com/download))
- ✅ **GitHub account** (free at github.com)
- ✅ **Text editor or IDE** (VS Code, IntelliJ, etc.)
- ✅ **ngrok account** (free at ngrok.com) - *if using local Jenkins*

### Verify Installation

```bash
# Check Docker
docker --version

# Check Docker Compose
docker-compose --version

# Check Git
git --version
```

---

## 🚀 Quick Start

### 1. Clone/Initialize Your Repository

If you don't have your code on GitHub yet:

```bash
# Option A: Clone an existing repo
git clone https://github.com/<your-username>/swag-labs-docker-tests
cd swag-labs-docker-tests

# Option B: Create a new repo
mkdir swag-labs-docker-tests
cd swag-labs-docker-tests
git init
git remote add origin https://github.com/<your-username>/swag-labs-docker-tests
```

### 2. Make Files Executable

```bash
chmod +x quickstart.sh
chmod +x docker-compose.yml
```

### 3. Run Quickstart Script

**If you have ngrok token:**
```bash
./quickstart.sh --with-ngrok --ngrok-token YOUR_NGROK_TOKEN
```

**If you don't have ngrok:**
```bash
./quickstart.sh
```

The script will:
- ✅ Check for Docker installation
- ✅ Start Jenkins container
- ✅ Display initial admin password
- ✅ Show next steps

### 4. Follow Onscreen Instructions

The script output will guide you through:
- Jenkins URL: `http://localhost:8080`
- Initial password
- Plugin installation
- Job creation steps

---

## 🔧 Manual Jenkins Setup

If you prefer to set up Jenkins manually:

### Step 1: Start Jenkins with Docker Compose

```bash
# Navigate to your project directory
cd /path/to/swag-labs-docker-tests

# Start Jenkins
docker-compose up -d

# View logs to see startup progress
docker-compose logs -f jenkins
```

Wait for the message:
```
Jenkins is fully up and running
```

### Step 2: Access Jenkins Web UI

Open your browser and go to:
```
http://localhost:8080
```

### Step 3: Retrieve Initial Admin Password

Run this command:
```bash
docker exec jenkins-ci cat /var/jenkins_home/secrets/initialAdminPassword
```

Copy the password and paste it in Jenkins.

### Step 4: Complete Initial Setup

1. **Paste Admin Password** - Copy from above
2. **Click "Continue"**
3. **Install Suggested Plugins** - Click this button
   - Jenkins will install common plugins automatically
4. **Wait** - Installation takes 5-10 minutes
5. **Create First Admin User** - Fill in the form
6. **Instance Configuration** - Keep defaults and click "Save and Continue"
7. **Start Using Jenkins** - Click "Start Using Jenkins"

---

## 📁 GitHub Repository Setup

### Step 1: Create a GitHub Repository

1. Go to [github.com/new](https://github.com/new)
2. **Repository name:** `swag-labs-docker-tests` (or your choice)
3. **Description:** `Automated test suite with Jenkins CI/CD`
4. **Visibility:** Public (easier for webhooks) or Private
5. Click **Create repository**

### Step 2: Push Your Code to GitHub

```bash
# Navigate to your local project
cd /path/to/swag-labs-docker-tests

# Add all files
git add .

# Commit
git commit -m "Initial commit: Swag Labs test automation with Jenkins"

# Push to GitHub (replace with your branch if not main)
git branch -M main
git push -u origin main
```

### Step 3: Verify on GitHub

Go to your repository on GitHub and verify all files are there:
- ✅ `Jenkinsfile`
- ✅ `docker-compose.yml`
- ✅ `Dockerfile`
- ✅ `pom.xml`
- ✅ `README.md`
- ✅ `src/` directory with test files

---

## 🔨 Create Jenkins Pipeline Job

### Step 1: Go to Jenkins Dashboard

Open `http://localhost:8080`

### Step 2: Create New Item

1. Click **New Item** (or New Job)
2. Enter job name: `Swag Labs Tests`
3. Select **Pipeline**
4. Click **OK**

### Step 3: Configure Job

The configuration page has several tabs. Fill in each section:

#### **General Tab**

- **Description:** "Automated test suite for Swag Labs with CI/CD integration"
- **GitHub project:** Leave unchecked (optional)

#### **Build Triggers Tab**

Check these boxes:
- ✅ **GitHub hook trigger for GITScm polling**
  - This enables automatic triggering on GitHub push
- ✅ **Poll SCM** - Set to `H/15 * * * *`
  - This checks for changes every 15 minutes as fallback

#### **Pipeline Tab**

1. **Definition:** Select **Pipeline script from SCM**
2. **SCM:** Select **Git**
3. **Repository URL:** 
   ```
   https://github.com/<your-username>/swag-labs-docker-tests.git
   ```
4. **Credentials:** 
   - If repository is **public**: Leave as "(none)"
   - If repository is **private**: Add credentials (see below)
5. **Branches to build:** 
   - Leave as `*/main` (default)
6. **Script Path:** 
   - Enter: `Jenkinsfile`
7. Advanced options → **Poll interval:** Leave blank

#### Add Credentials (if private repo)

1. Click **Add** next to Credentials dropdown
2. Select **Jenkins** from dropdown
3. **Kind:** "Username with password"
4. **Username:** Your GitHub username
5. **Password:** Your GitHub personal access token (or password)
6. **ID:** `github-credentials`
7. Click **Add**
8. Select your new credentials from dropdown

### Step 4: Save Configuration

Click **Save**

---

## 🔗 Configure GitHub Webhooks

Webhooks allow GitHub to automatically trigger Jenkins when you push code.

### Option 1: With ngrok (Local Development)

If you used ngrok in the quickstart:

1. Check ngrok public URL:
   ```bash
   curl http://localhost:4040/api/tunnels
   ```
   Look for `"public_url": "https://xxxxx.ngrok.io"`

2. In GitHub repository:
   - Settings → Webhooks → Add webhook
   - **Payload URL:** `https://xxxxx.ngrok.io/github-webhook/`
   - **Content type:** `application/json`
   - **Events:** `Push events`
   - Click **Add webhook**

### Option 2: Without ngrok (Public Server)

If Jenkins is on a public server:

1. In GitHub repository:
   - Settings → Webhooks → Add webhook
   - **Payload URL:** `http://<your-server-ip>:8080/github-webhook/`
   - **Content type:** `application/json`
   - **Events:** Select "Just the push event"
   - Click **Add webhook**

### Verify Webhook

In GitHub, go to Settings → Webhooks and click your webhook:
- Check **Recent Deliveries** tab
- Should see successful deliveries (green checkmarks)
- If red X, click to see error details

---

## 🌍 Ngrok Setup (for local machines)

Ngrok exposes your local Jenkins to the internet so GitHub can reach it.

### Step 1: Create Ngrok Account

1. Go to [ngrok.com](https://ngrok.com)
2. Sign up (free)
3. Get your authtoken from dashboard

### Step 2: Add Ngrok Token to Environment

Create `.env` file in your project directory:

```bash
# .env
NGROK_AUTHTOKEN=your_token_here
```

### Step 3: Start Ngrok with Docker Compose

```bash
# Start Jenkins with ngrok
docker-compose --profile ngrok up -d

# Check ngrok URL
docker logs ngrok-tunnel | grep "forwarding"
```

Output will show:
```
https://xxxxx.ngrok.io -> http://jenkins-ci:8080
```

### Step 4: Use Ngrok URL for GitHub Webhook

Use the ngrok URL (e.g., `https://xxxxx.ngrok.io`) in your GitHub webhook configuration.

### Notes on Ngrok

- **Free tier** - URL changes every time you restart (update webhook manually)
- **Paid tier** - Static URL (no need to update)
- Dashboard available at `http://localhost:4040`
- Tunnel shows all traffic for debugging

---

## 📦 Install Jenkins Plugins

Some plugins are installed by default with "Install suggested plugins". Ensure these are installed:

### Required Plugins

1. **Pipeline** - For declarative pipelines ✅ (default)
2. **Git** - For Git integration ✅ (default)
3. **GitHub** - For GitHub-specific features ✅ (default)
4. **HTML Publisher** - To publish HTML reports
5. **JUnit** - To parse JUnit test results ✅ (default)
6. **Email Extension** - For email notifications
7. **Timestamper** - Adds timestamps to logs

### Optional Plugins

- **Slack** - Slack notifications
- **Blue Ocean** - Modern UI for pipelines
- **AnsiColor** - Colored output in logs

### Install Plugins

1. Go to **Manage Jenkins** → **Plugin Manager**
2. Click **Available** tab
3. Search for plugin name
4. Check the checkbox
5. Click **Install without restart** at bottom
6. When done, click **Manage Jenkins** → **Restart Jenkins**

---

## ✅ Test Your Pipeline

### Manual Trigger

1. Go to your job in Jenkins
2. Click **Build Now**
3. Watch the build in **Build History** on left
4. Click build number to see detailed logs

### Check Console Output

1. Click the build number (e.g., #1)
2. Click **Console Output**
3. Should see:
   - Checkout Code
   - Build
   - Download Dependencies
   - Run Tests
   - Generate HTML Reports
   - Archive Reports

### Expected Output

```
Started by user [username]
Building in workspace /var/jenkins_home/workspace/Swag Labs Tests

[*] Running in workspace: /var/jenkins_home/workspace/Swag Labs Tests

[Swag Labs Tests] $ git clone ...
[Swag Labs Tests] $ git checkout ...

========== Checking out code from repository ==========
...
========== Building project with Maven ==========
[INFO] BUILD SUCCESS
...
========== Running test suite ==========
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
...
========== Archiving test reports ==========
Archiving artifacts
BUILD SUCCESS
```

### View Test Results

After successful build:

1. Go to job page (click job name)
2. Look for **Test Result** section - shows pass/fail summary
3. Click **Latest Test Result** to see detailed results

---

## 🔄 Test with GitHub Push

### Step 1: Make a Small Change

```bash
# In your project directory
echo "# Test update" >> README.md
git add README.md
git commit -m "Test webhook trigger"
git push origin main
```

### Step 2: Watch Jenkins

1. Go to Jenkins job page
2. **Build History** should show new build starting
3. (Or refresh page and check **Recent Changes**)

### Step 3: View Results

When build completes:
- Check console output
- View test results
- Download artifacts if needed

---

## 🎨 Advanced Configuration

### Modify Pipeline to Use Docker

To run tests in Docker container instead of Jenkins host:

In `Jenkinsfile`, replace `agent any` with:

```groovy
agent {
    docker {
        image 'maven:3.9.6-eclipse-temurin-17'
        args '-v /var/run/docker.sock:/var/run/docker.sock'
    }
}
```

### Add Email Notifications

In `Jenkinsfile`, uncomment in `post` section:

```groovy
failure {
    emailext(
        subject: "Build Failed: ${JOB_NAME} #${BUILD_NUMBER}",
        body: """
        Build ${BUILD_NUMBER} failed.
        Check: ${BUILD_URL}
        Commit: ${GIT_COMMIT}
        """,
        to: 'team@example.com'
    )
}
```

Configure mail server in **Manage Jenkins** → **System** → **Email Notification**.

### Add Slack Integration

1. In Slack workspace, create an incoming webhook
2. In Jenkins: **Manage Jenkins** → **System** → **Slack** → Add token
3. In `Jenkinsfile`:

```groovy
success {
    slackSend(
        color: 'good',
        message: "✅ Build #${BUILD_NUMBER} passed\n${BUILD_URL}"
    )
}
failure {
    slackSend(
        color: 'danger',
        message: "❌ Build #${BUILD_NUMBER} failed\n${BUILD_URL}"
    )
}
```

### Parallel Test Stages

Run multiple test suites in parallel:

```groovy
parallel {
    stage('UI Tests') {
        steps {
            sh 'mvn test -Dgroups=ui'
        }
    }
    stage('API Tests') {
        steps {
            sh 'mvn test -Dgroups=api'
        }
    }
}
```

### Add Approval Step

Require manual approval before deploying:

```groovy
stage('Approve Deployment') {
    steps {
        input 'Deploy to production?'
    }
}
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Cannot connect to Docker daemon"

**Error:** `Cannot connect to the Docker daemon`

**Solution:**
```bash
# Start Docker Desktop (if on Mac/Windows)
# Or for Linux, start Docker service:
sudo systemctl start docker
sudo usermod -aG docker $USER
newgrp docker
```

### Issue 2: "Port 8080 already in use"

**Error:** `bind address already in use`

**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change Jenkins port in docker-compose.yml:
# ports:
#   - "8081:8080"  # Use 8081 instead
```

### Issue 3: "GitHub webhook not working"

**Check:**
1. GitHub webhook delivery logs (Settings → Webhooks)
2. Jenkins logs: `docker-compose logs jenkins`
3. Ngrok is running if using local Jenkins
4. Payload URL is correct and accessible

### Issue 4: "Git credential fails"

**Solution:**
```bash
# If using HTTPS, create GitHub personal access token:
# Go to GitHub → Settings → Developer settings → Personal access tokens
# Create token with 'repo' scope

# Use token as password in Jenkins credentials
```

### Issue 5: "Tests timeout in pipeline"

**Solution:** Increase timeout in `Jenkinsfile`:
```groovy
-Dmaven.surefire.timeout=1200  # Increase to 20 minutes
```

---

## 📊 Monitoring Your Builds

### Jenkins Dashboard

- **Main page** shows all jobs and last build status
- **Trend graph** shows build history
- **Build queue** shows pending builds

### Blue Ocean (Optional Modern UI)

Install **Blue Ocean** plugin for better visualization:

1. **Manage Jenkins** → **Plugin Manager**
2. Search for "Blue Ocean"
3. Install without restart
4. Click **Open Blue Ocean** on left sidebar

---

## 🔒 Security Considerations

### Jenkins Security

1. **Change default password** - Create strong admin password
2. **Limit anonymous access** - **Manage Jenkins** → **Security**
3. **Use HTTPS** - Put Jenkins behind reverse proxy (nginx)
4. **Use credentials** - Never hardcode passwords in Jenkinsfile
5. **Regular updates** - Keep Jenkins and plugins updated

### GitHub Security

1. **Use personal access token** - Not your main GitHub password
2. **Restrict token scope** - Only "repo" access needed
3. **Rotate tokens** - Every 90 days
4. **Use protected branches** - Require reviews before merge

### ngrok Security

1. **Don't share ngrok URL** - It exposes your Jenkins
2. **Use ngrok firewall rules** - If on paid plan
3. **Change Jenkins password** - As mentioned above
4. **Monitor ngrok access logs** - Check for unauthorized access

---

## 📚 Next Steps

1. **Customize pipeline** - Modify stages for your needs
2. **Add notifications** - Slack, Email, or custom webhooks
3. **Implement code quality** - Add SonarQube or Checkstyle
4. **Expand test suite** - Add more test cases
5. **Multi-branch builds** - Build from multiple branches
6. **Staging/Production** - Extend pipeline to deployment stages

---

## 📞 Support & Resources

- **Jenkins Documentation:** https://www.jenkins.io/doc/
- **Declarative Pipeline:** https://www.jenkins.io/doc/book/pipeline/syntax/
- **GitHub Webhooks:** https://docs.github.com/en/developers/webhooks-and-events/webhooks/
- **Docker Compose:** https://docs.docker.com/compose/
- **Ngrok Docs:** https://ngrok.com/docs/

---

**Happy automating! 🚀**
