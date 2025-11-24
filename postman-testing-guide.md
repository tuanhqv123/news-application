# Firebase Cloud Messaging (HTTP v1 API) Postman Testing Guide

This guide will help you test Firebase push notifications using Postman with the modern HTTP v1 API.

## Prerequisites

1. **Firebase Project Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project or create a new one
   - Ensure Cloud Messaging API (V1) is enabled
   - Sender ID: `702340089040`

2. **Get Service Account Credentials**
   - Go to Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file (save as `service-account-key.json`)
   - **Keep this key secure - it gives admin access to your Firebase project!**

3. **Get FCM Token**
   - Install and run your app
   - Navigate to FirebaseSetupActivity
   - Click "Get FCM Token" button
   - Token will be displayed and copied to clipboard

## Postman Setup - Method 1: Manual OAuth2 Token

### 1. Get OAuth2 Access Token
First, get an OAuth2 access token using your service account key:

**URL:** `https://oauth2.googleapis.com/token`
**Method:** **POST**
**Headers:** `Content-Type: application/x-www-form-urlencoded`
**Body (x-www-form-urlencoded):**
```
grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer
assertion=YOUR_JWT_TOKEN
```

**OR use this curl command to get the token:**
```bash
# Install Google Cloud SDK first
gcloud auth activate-service-account --key-file=service-account-key.json
gcloud auth print-access-token
```

### 2. Create FCM Request
**Method:** **POST**
**URL:** `https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send`
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

## Postman Setup - Method 2: Using OAuth2 Helper (Recommended)

### 1. Install Google Cloud CLI
```bash
# On macOS with Homebrew
brew install google-cloud-sdk
gcloud init
```

### 2. Authenticate
```bash
gcloud auth activate-service-account --key-file=service-account-key.json
```

### 3. Get Access Token
```bash
gcloud auth print-access-token
```

### 4. Use the token in Postman
The token is valid for 1 hour, so you'll need to refresh it periodically.

## Request Body Format (HTTP v1 API)

### Important: Get Your Project ID
Your project ID can be found in Firebase Console → Project Settings → General

### Single Device Notification
```json
{
  "message": {
    "token": "YOUR_FCM_TOKEN_HERE",
    "notification": {
      "title": "Breaking News",
      "body": "New article just published!"
    },
    "android": {
      "priority": "high",
      "notification": {
        "click_action": "com.example.newsapplication.MainActivity",
        "icon": "@drawable/ic_notification"
      }
    }
  }
}
```

### Advanced Notification with Data
```json
{
  "message": {
    "token": "YOUR_FCM_TOKEN_HERE",
    "notification": {
      "title": "Breaking News",
      "body": "New article about technology trends"
    },
    "data": {
      "title": "Technology News",
      "body": "Latest developments in AI and machine learning",
      "article_url": "https://example.com/article/123",
      "category": "technology",
      "author": "John Doe"
    },
    "android": {
      "priority": "high",
      "notification": {
        "click_action": "com.example.newsapplication.ArticleDetailActivity",
        "icon": "@drawable/ic_notification",
        "sound": "default",
        "color": "#FF0000"
      }
    }
  }
}
```

### Topic-based Notification
```json
{
  "message": {
    "topic": "news_updates",
    "notification": {
      "title": "Daily News Update",
      "body": "Check out today's top stories"
    },
    "data": {
      "article_count": "5",
      "category": "all"
    }
  }
}
```

### Multicast (Multiple Devices)
```json
{
  "message": {
    "tokens": [
      "FCM_TOKEN_1",
      "FCM_TOKEN_2",
      "FCM_TOKEN_3"
    ],
    "notification": {
      "title": "Breaking News Alert",
      "body": "Multiple recipients notification test"
    }
  }
}
```

## Testing Scenarios

### 1. Single Device Notification
- Use your device's FCM token
- Test notification display
- Check notification behavior when tapped

### 2. Topic Subscription
- In app: Subscribe to "news_updates" topic
- Send to `/topics/news_updates`
- Verify all subscribed devices receive notification

### 3. Silent Push with Data
```json
{
  "to": "YOUR_FCM_TOKEN_HERE",
  "data": {
    "silent_update": "true",
    "refresh_data": "articles",
    "category": "technology"
  }
}
```

## Response Examples (HTTP v1 API)

### Success Response (200 OK)
```json
{
  "name": "projects/YOUR_PROJECT_ID/messages/1234567890abcdef"
}
```

### Error Response (400 Bad Request)
```json
{
  "error": {
    "code": 400,
    "message": "Invalid token",
    "status": "INVALID_ARGUMENT",
    "details": [
      {
        "@type": "type.googleapis.com/google.firebase.fcm.v1.FcmError",
        "errorCode": "UNREGISTERED"
      }
    ]
  }
}
```
**Solution:** Get a fresh FCM token from the app

### Error Response (401 Unauthorized)
```json
{
  "error": {
    "code": 401,
    "message": "Request had invalid authentication credentials.",
    "status": "UNAUTHENTICATED"
  }
}
```
**Solution:** Refresh your OAuth2 access token

### Error Response (403 Forbidden)
```json
{
  "error": {
    "code": 403,
    "message": "The caller does not have permission",
    "status": "PERMISSION_DENIED"
  }
}
```
**Solution:** Check service account permissions in Firebase Console

## Android Debugging

### 1. Check Logcat
Filter for these tags:
- `MyFirebaseMsgService`
- `FirebaseMessaging`
- `FirebaseManager`

### 2. Common Issues
- **Notifications not showing:** Check notification permissions (Android 13+)
- **Token not updating:** Clear app data and restart
- **"InvalidRegistration":** Get a fresh FCM token

### 3. Notification Permissions (Android 13+)
The app will automatically request permission. You can check in:
Settings → Apps → News Application → Notifications

## Advanced Features

### Custom Notification Icons
Add to notification payload:
```json
{
  "notification": {
    "icon": "@drawable/ic_notification_icon"
  }
}
```

### Notification Channels (Android 8+)
The app automatically creates "news_notification_channel" channel.

### Deep Linking
Use data payload with custom URL scheme:
```json
{
  "data": {
    "deep_link": "newsapp://article/123"
  }
}
```

## Security Best Practices

1. **Never expose Server Key in client-side code**
2. **Use HTTPS for all FCM requests**
3. **Validate input data before using it**
4. **Consider using a backend service to send notifications**

## Testing Checklist

- [ ] FCM token generated successfully
- [ ] Basic notification received
- [ ] Notification with data payload works
- [ ] Topic subscription works
- [ ] App handles notification clicks
- [ ] Notifications work in background
- [ ] Permissions requested (Android 13+)
- [ ] Logcat shows no critical errors

## Troubleshooting

### Device Not Receiving Notifications
1. Check internet connection
2. Verify FCM token is current
3. Check notification permissions
4. Look at Logcat for errors
5. Test with different payload

### Postman Errors
1. Verify server key is correct
2. Check JSON syntax
3. Ensure headers are properly set
4. Test with simpler payloads first

For more details, refer to [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging).