# 🚀 Quick Action - Fix Notifications (5 Minutes)

## The Problem
Slack and Email notifications are not being sent because:
- ❌ Slack webhook credential not configured in Jenkins
- ❌ Email SMTP settings not configured in Jenkins

## The Solution

### Step 1: Add Slack Webhook Credential (2 minutes)

1. Open Jenkins: `http://localhost:8080`
2. Click **Manage Jenkins** → **Manage Credentials** → **System** → **Global credentials**
3. Click **+ Add Credentials**
4. Fill in:
   - **Kind**: `Secret text`
   - **Secret**: Your Slack webhook URL (from https://api.slack.com/apps)
   - **ID**: `slack-webhook-url` ⚠️ EXACT!
   - **Description**: `Slack Webhook for Build Notifications`
5. Click **Create**

### Step 2: Configure Email SMTP (3 minutes)

1. Click **Manage Jenkins** → **System**
2. Find **"Email Notification"** section
3. Fill in (**Gmail example**):
   - **SMTP server**: `smtp.gmail.com`
   - **SMTP port**: `587`
   - **Username**: Your Gmail address
   - **Password**: 16-char app password (from https://myaccount.google.com/apppasswords)
   - **Use SMTP Authentication**: ✅
   - **Use TLS**: ✅
   - **SMTP TLS port**: `587`
   - **Default user e-mail suffix**: `@amalitechtraining.org`
4. Click **Test configuration by sending test e-mail** and verify
5. Click **Save**

### Step 3: Test (Immediate)

1. Go to your Jenkins job
2. Click **Build Now**
3. Wait for completion
4. Check:
   - 📧 Email inbox for: `✅ BUILD PASSED: jenkins-swag-labs #1`
   - 💬 Slack #builds channel for: `✅ BUILD PASSED`
   - 📋 Jenkins console for: `✅ Slack notification sent successfully`

---

## ✅ Expected Results

After next build completes, you should see:

**Email**:
```
Subject: ✅ BUILD PASSED: jenkins-swag-labs #1
To: hasbiyallah.umutoniwabo@amalitechtraining.org
```

**Slack**:
```
✅ BUILD PASSED
Job: jenkins-swag-labs
Build: #1
Branch: main
```

**Jenkins Console**:
```
✅ Slack notification sent successfully
✅ Email notification sent successfully
```

---

## 🆘 If Something Doesn't Work

**Email not arriving?**
- Is SMTP server configured? (gmail: `smtp.gmail.com:587`)
- Did you use app password (Gmail) or regular password (Outlook)?
- Check Jenkins System Log for SMTP errors

**Slack not sending?**
- Is credential ID exactly `slack-webhook-url`?
- Is the webhook URL correct?
- Check if `#builds` channel exists in Slack

**Both issues?**
- Restart Jenkins: `docker restart jenkins`
- Verify credentials exist
- Try another build

---

## 📖 Full Documentation

For detailed setup instructions, see: `JENKINS_SETUP_COMPLETE.md`

---

**Time required**: ~5 minutes
**Difficulty**: Easy ✅
**Next step**: Configure above → Build → Verify notifications
