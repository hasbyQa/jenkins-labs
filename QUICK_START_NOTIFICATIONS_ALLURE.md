# Quick Start: Notifications & Allure Reports

## TL;DR - Get Started Now

### For Slack Notifications ⚡

1. **Create Slack webhook** (5 minutes)
   - Go to https://api.slack.com/apps → Create New App
   - Set up Incoming Webhooks
   - Copy webhook URL

2. **Configure Jenkins** (2 minutes)
   - Jenkins → Manage Jenkins → System
   - Find "Slack" section
   - Paste webhook URL
   - Test connection
   - Save

3. **Done!** ✅
   - Next build will send Slack messages
   - Success = green ✅, Failure = red ❌

### For Email Notifications 📧

1. **Gmail setup** (5 minutes)
   - Enable 2-factor auth: https://myaccount.google.com/security
   - Create App Password: https://myaccount.google.com/apppasswords
   - Copy the 16-character password

2. **Configure Jenkins** (2 minutes)
   - Jenkins → Manage Jenkins → System
   - Find "Email Notification" section
   - Server: `smtp.gmail.com`
   - Port: `587`
   - Username: your email
   - Password: the 16-character app password (NOT your Gmail password)
   - Default suffix: `@gmail.com`
   - Click "Test configuration by sending test e-mail"
   - Save

3. **Done!** ✅
   - Check your email inbox for test message
   - Next build will send emails

### For Allure Reports 📊

**Already configured!** Just run tests:

```bash
# Run tests
mvn clean test

# Generate Allure report
mvn allure:report

# View locally
open target/site/allure-maven-plugin/index.html
```

In Jenkins:
- After each build, click "Allure Report" link
- Beautiful dashboard with test results
- History graph showing trend
- Automatic screenshots on failure

## Verify Everything Works

### 1. Test Locally First

```bash
# Build project
mvn clean compile

# Run tests (generates Allure results)
mvn test

# Generate Allure report
mvn allure:report

# View report
open target/site/allure-maven-plugin/index.html
```

Expected: All 19 tests pass ✅

### 2. Test in Jenkins

```bash
# Push changes to GitHub
git add .
git commit -m "Add Allure reports and fix notifications"
git push

# Jenkins automatically builds (GitHub webhook)
# Check Jenkins job for:
#   - ✅ Build Success
#   - 📧 Email notification received
#   - 💬 Slack message in channel
#   - 📊 Allure Report link in build
```

### 3. View Allure Report in Jenkins

1. Open Jenkins job page
2. Click on latest build number
3. Scroll to bottom, click **"Allure Report"**
4. Interactive dashboard loads!

## Troubleshooting Quick Fixes

### Slack Not Sending?

```
✅ Do you have Slack Plugin installed?
   → Manage Jenkins → Manage Plugins → Search "Slack" → Install

✅ Is webhook URL correct?
   → Jenkins → System → Slack section → Check URL
   
✅ Is Jenkins restarted?
   → After plugin install, restart: sudo systemctl restart jenkins
```

### Email Not Sending?

```
✅ Did you use APP PASSWORD (not Gmail password)?
   → Gmail: Use 16-char app password from https://myaccount.google.com/apppasswords
   → Outlook: Same concept, create app password
   
✅ Check SMTP settings:
   → Jenkins → System → Email Notification
   → Server: smtp.gmail.com
   → Port: 587 (NOT 25 or 465 for most cases)
   
✅ Test manually:
   → Jenkins → System → Email → Test configuration
```

### Allure Report Not Showing?

```
✅ Did tests run successfully?
   → Check build logs for "BUILD SUCCESS"
   
✅ Is HTML Publisher Plugin installed?
   → Manage Jenkins → Manage Plugins → Search "HTML Publisher"
   
✅ Check report location:
   → Jenkins logs should show where report generated
   → Should be in: target/site/allure-maven-plugin/index.html
```

## File Changes Summary

### Modified Files

| File | Changes |
|------|---------|
| `pom.xml` | Added Allure JUnit5 dependency, Allure Maven plugin, Surefire listener config |
| `Jenkinsfile` | Added Allure report generation, improved notifications with error handling |

### New Documentation Files

| File | Purpose |
|------|---------|
| `NOTIFICATIONS_SETUP.md` | Detailed setup for Slack & Email |
| `ALLURE_REPORTS.md` | Complete Allure reports guide |
| `QUICK_START_NOTIFICATIONS_ALLURE.md` | This file! |

## Next Steps

1. ✅ **Configure Slack webhook** (if using Slack)
2. ✅ **Configure Email SMTP** (if using Email)
3. ✅ **Install Slack Plugin** (if not already)
4. ✅ **Install HTML Publisher Plugin** (if not already)
5. ✅ **Push code to GitHub**
6. ✅ **Watch Jenkins build trigger**
7. ✅ **Verify notifications received**
8. ✅ **Check Allure Report in Jenkins**

## Still Not Working?

### Check Jenkins Logs

```bash
# SSH into Jenkins container
docker exec -it jenkins-ci bash

# View recent logs
tail -f /var/jenkins_home/logs/all.log

# Or from Jenkins UI:
# Manage Jenkins → System Log → Recent Logs
```

### Test Each Notification Separately

Create a test job to verify:

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                echo 'Testing notifications...'
            }
        }
    }
    post {
        always {
            // Test Slack
            slackSend(
                channel: '#builds',
                message: 'Test Slack message'
            )
            // Test Email
            emailext(
                subject: 'Test Email',
                body: 'This is a test email from Jenkins',
                to: 'your-email@gmail.com'
            )
        }
    }
}
```

### Common Issues & Fixes

| Problem | Solution |
|---------|----------|
| "Invalid webhook URL" | Re-copy from Slack API, ensure no spaces |
| "SMTP Authentication failed" | Use app password, not Gmail password |
| "Connection timeout" | Check firewall allows port 587, try port 465 |
| "Plugin not found" | Install plugin, restart Jenkins |
| "Report not published" | Check job is Pipeline type, not Freestyle |

---

**Questions?** See `NOTIFICATIONS_SETUP.md` or `ALLURE_REPORTS.md` for detailed guides!

**Ready to go!** 🚀
