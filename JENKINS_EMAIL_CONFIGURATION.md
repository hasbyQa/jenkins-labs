# Jenkins Email Configuration Guide

## Issue: "Not sent to the following valid addresses"

The error occurs because the Jenkins mail() step uses the **Mailer Plugin** which requires proper SMTP configuration at the Jenkins System level.

---

## ✅ SOLUTION: Configure Jenkins Mailer Plugin

### Step 1: Go to Jenkins System Configuration

1. Click **Manage Jenkins** (top left)
2. Click **System** (under System Configuration)
3. Scroll to **E-mail Notification** section

### Step 2: Configure Email Settings

Fill in the following fields:

```
SMTP server: smtp.gmail.com
SMTP port: 587
Default user email suffix: @gmail.com
```

### Step 3: Enable TLS/SSL

- ☑️ Check "Use SMTP Authentication"
- ☑️ Check "Use TLS"

Username and Password:
```
Username: hasbiyallah.umutoniwabo@amalitechtraining.org
Password: tmzw rruc eyco iyty
```

**IMPORTANT:** The password should NOT have spaces. Use: `tmzwrruceycoyity` (spaces removed)

### Step 4: Test Email Configuration

1. Click **"Test configuration by sending test e-mail"**
2. Enter test email: `hasbiyallah.umutoniwabo@amalitechtraining.org`
3. Click **"Test e-mail"**
4. You should see: `✅ Email was successfully sent`

### Step 5: Save Configuration

Click **"Save"** at the bottom of the page.

---

## Complete Jenkins System Configuration

```
Jenkins System Configuration > E-mail Notification

├── SMTP server: smtp.gmail.com
├── SMTP port: 587
├── Default user email suffix: @gmail.com
├── Use SMTP Authentication: ✓ CHECKED
│   ├── Username: hasbiyallah.umutoniwabo@amalitechtraining.org
│   └── Password: tmzwrruceycoyity  (NO SPACES)
├── Use TLS: ✓ CHECKED
└── [Test e-mail button available]

Reply-To Address: (leave blank or use default)
Charset: UTF-8
```

---

## Jenkinsfile Changes Made

The Jenkinsfile now uses the **mail()** step instead of **emailext()**:

### Before (Not Working)
```groovy
emailext(
    to: 'user@example.com',
    recipientList: 'user@example.com',  // ❌ Invalid parameter
    mimeType: 'text/plain'
)
```

### After (Working) ✅
```groovy
mail(
    subject: "✅ BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
    body: "...",
    to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
    from: 'jenkins@localhost',
    charset: 'UTF-8'
)
```

**Key Changes:**
- ✅ Uses `mail()` step (Mailer Plugin)
- ✅ Uses `from: 'jenkins@localhost'`
- ✅ Proper charset encoding
- ✅ Simple parameters (no `recipientList`)

---

## Detailed Reports Generated

### 1. Allure Test Report
- **Location:** Jenkins Job > Allure Test Report
- **Contains:**
  - Overall test statistics (passed, failed, skipped)
  - Test execution timeline
  - Individual test details with logs
  - Screenshots and artifacts
  - Historical trends

### 2. JUnit Test Results
- **Location:** Jenkins Job > Test Result
- **Contains:**
  - Pass/fail summary
  - Test execution time
  - Stack traces for failed tests

### 3. Email Notifications
- **On Success:** Includes links to all reports
- **On Failure:** Includes troubleshooting steps and report links

### 4. Slack Notifications
- **On Success:** Green notification with report links
- **On Failure:** Red notification with action items

---

## Testing the Setup

### 1. Verify Email Configuration
```
Jenkins > Manage Jenkins > System > E-mail Notification > Test e-mail
```
Expected: `✅ Email was successfully sent`

### 2. Trigger a Build
```
Jenkins > Your Job > Build Now
```

### 3. Check Email
- Subject: `✅ BUILD PASSED: jenkins_lab #X`
- Contains: Report links and detailed information

### 4. Check Slack
- Message in `#builds` channel
- Contains: Build details and report links

### 5. View Detailed Reports
- Click **Allure Test Report** link in Jenkins UI
- Click **Test Result** link for JUnit
- Review **Console Output** for debug info

---

## Troubleshooting

### Issue: Still Getting "Not sent to the following valid addresses"

**Cause:** SMTP not configured correctly

**Fix:**
1. Go to Jenkins System Configuration
2. Verify all fields are filled correctly
3. **Remove spaces from password:** `tmzw rruc eyco iyty` → `tmzwrruceycoyity`
4. Enable TLS
5. Test configuration

### Issue: "No SMTP server specified"

**Cause:** Email notification settings not configured

**Fix:**
1. Go to Jenkins System Configuration
2. Scroll to "E-mail Notification"
3. Enter SMTP server: `smtp.gmail.com`
4. Save configuration

### Issue: Gmail authentication failed

**Cause:** Using regular Gmail password or incorrect username

**Fix:**
1. Use Gmail App Password: `tmzwrruceycoyity` (without spaces)
2. Ensure 2-Factor Authentication is enabled on Gmail
3. Generate new app password if needed

---

## Email Content Examples

### Success Email
```
BUILD SUCCESSFULLY COMPLETED ✅

🎯 JOB DETAILS:
   Job Name: jenkins_lab
   Build Number: #5
   Status: ✅ SUCCESS
   Branch: main
   Commit: abc123def456

📊 DETAILED REPORTS AVAILABLE:
   1. Allure Test Report: [LINK]
   2. JUnit Test Results: [LINK]
   3. Build Console Output: [LINK]
```

### Failure Email
```
BUILD FAILED ❌

❌ TEST RESULTS:
   Some tests failed or build encountered errors!

📊 DETAILED REPORTS & DIAGNOSTIC INFORMATION:
   1. Allure Test Report (with failed test details): [LINK]
   2. JUnit Test Results: [LINK]
   3. Build Console Output (error messages): [LINK]

🔍 TROUBLESHOOTING STEPS:
   Step 1: Check Console Output
   Step 2: Review Failed Tests in Allure
   Step 3: Check Test Logs
```

---

## Next Steps

1. **Configure Jenkins Email** (follow Step 1-5 above)
2. **Test Email Configuration** (use Test e-mail button)
3. **Trigger Build:** Click "Build Now"
4. **Verify Notifications:** Check email and Slack
5. **Review Reports:** Click report links in Jenkins

---

## Jenkins Configuration Files

The configuration is now in: `Jenkinsfile`

Key sections:
- **Lines 8-10:** Environment variables
- **Lines 35-39:** Allure report generation
- **Lines 41-48:** HTML report publishing
- **Lines 57-93:** Success email + Slack (with detailed reports)
- **Lines 95-128:** Failure email + Slack (with troubleshooting)

All changes are version controlled and ready to push to GitHub!

---

## Final Commands

Once email is configured:

```bash
# Commit changes
git add Jenkinsfile
git commit -m "Add mail() step with detailed reports and Slack notifications"

# Push to GitHub
git push origin main

# Build will trigger automatically via webhook
# Check email and Slack for notifications
```

---

**Questions?** Run `mvn allure:report` locally to test Allure report generation!
