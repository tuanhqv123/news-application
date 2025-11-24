# Firebase Cloud Messaging (HTTP v1 API) Quick Setup

This guide will help you quickly set up and test Firebase push notifications with your News Application.

## 🚀 Quick Start

### 1. Get Your Service Account Key
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. **Project Settings → Service Accounts**
4. Click **"Generate new private key"**
5. Save as `service-account-key.json` in project root

### 2. Get OAuth2 Access Token (One-time setup)
```bash
# Install Node.js dependencies
npm install

# Generate access token
npm run token
```
**The token is valid for 1 hour** - regenerate when needed

### 3. Get Device FCM Token
1. Build and run the app
2. Open FirebaseSetupActivity (add button to MainActivity)
3. Click **"Get FCM Token"**
4. Token is copied to clipboard

### 4. Send Test Notification with Postman

**Setup:**
- **Method:** POST
- **URL:** `https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send`
- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_OAUTH2_TOKEN`

**Body (JSON):**
```json
{
  "message": {
    "token": "DEVICE_FCM_TOKEN_HERE",
    "notification": {
      "title": "News Alert! 📰",
      "body": "Breaking news just in..."
    },
    "data": {
      "article_url": "https://example.com/news/123"
    }
  }
}
```

## 📱 Adding FirebaseSetupActivity to Your App

In your MainActivity.kt:
```kotlin
// Add this method
private fun openFirebaseSetup() {
    val intent = Intent(this, FirebaseSetupActivity::class.java)
    startActivity(intent)
}

// Call this method from a button or menu item
```

In your activity_main.xml, add a button:
```xml
<Button
    android:id="@+id/firebaseSetupButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Test Firebase Setup"
    android:layout_margin="16dp" />
```

## 🎯 Testing Checklist

- [ ] Service account key downloaded
- [ ] OAuth2 access token generated
- [ ] App installed on device
- [ ] FCM token retrieved
- [ ] Test notification sent via Postman
- [ ] Notification received on device
- [ ] Notification click handled properly

## 🔍 Common Issues & Solutions

### 401 Unauthorized
- **Problem:** Expired OAuth2 token
- **Solution:** Run `npm run token` again

### 400 Bad Request - Invalid Token
- **Problem:** Device FCM token expired
- **Solution:** Get fresh token from app

### Notification Not Showing
- **Problem:** Notification permission (Android 13+)
- **Solution:** App requests permission automatically, check Settings

## 📚 Advanced Features

### Topic-Based Notifications
```json
{
  "message": {
    "topic": "news_updates",
    "notification": {
      "title": "Daily News",
      "body": "Today's top stories"
    }
  }
}
```

### Rich Notifications
```json
{
  "message": {
    "token": "DEVICE_TOKEN",
    "notification": {
      "title": "Breaking News",
      "body": "Major tech announcement",
      "image": "https://example.com/news-image.jpg"
    },
    "android": {
      "notification": {
        "icon": "@drawable/ic_news",
        "color": "#FF0000",
        "sound": "default",
        "click_action": "com.example.newsapplication.ArticleDetailActivity"
      }
    },
    "data": {
      "article_id": "123",
      "category": "technology"
    }
  }
}
```

## 🛠️ Debug Tips

**Check Logcat for:**
- `MyFirebaseMsgService` - FCM service logs
- `FirebaseMessaging` - Token generation logs
- `FirebaseManager` - Token management logs

**Postman Response Success:**
```json
{
  "name": "projects/YOUR_PROJECT_ID/messages/1234567890abcdef"
}
```

## 📄 Files Created/Modified

- `app/src/main/java/com/example/newsapplication/firebase/` - Firebase services
- `app/src/main/java/com/example/newsapplication/FirebaseSetupActivity.kt` - Testing UI
- `app/src/main/res/layout/activity_firebase_setup.xml` - Layout
- `get-fcm-token.js` - OAuth2 token generator
- `postman-testing-guide.md` - Detailed testing guide
- `package.json` - Node.js dependencies

**🎉 You're all set! Your app now supports Firebase Cloud Messaging with the modern HTTP v1 API.**