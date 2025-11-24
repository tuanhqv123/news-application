# 🎯 SIMPLE POSTMAN TESTING GUIDE (NO OAUTH2!)

## 📱 Your Device FCM Token:
```
fuMl_SRlQfa_hcnwl-RA8j:APA91bHKdJV-ytAhIo3_RXsmOfoIYFLHGkWCT4N2bByVLH6igSUY-zIa7B54hEQsrMrsKn_nxsnOJs1132mc1d-p5pmFUhBaV2vRZhqpzMLeyn5neEIJiy0
```

## 🔑 Get Server Key (NOT OAuth2!)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `newsapplication-877fc`
3. **Project Settings → Cloud Messaging**
4. Look for **"Server Key"** (starts with `AAAA...`)
5. Copy this key!

## 📮 Postman Setup (LEGACY API - SUPER SIMPLE!)

**Method:** POST
**URL:** `https://fcm.googleapis.com/fcm/send`

**Headers:**
```
Content-Type: application/json
Authorization: key=AAAA_SERVER_KEY_HERE
```

**Body (JSON):**
```json
{
  "to": "fuMl_SRlQfa_hcnwl-RA8j:APA91bHKdJV-ytAhIo3_RXsmOfoIYFLHGkWCT4N2bByVLH6igSUY-zIa7B54hEQsrMrsKn_nxsnOJs1132mc1d-p5pmFUhBaV2vRZhqpzMLeyn5neEIJiy0",
  "notification": {
    "title": "Breaking News! 📰",
    "body": "New article published in NewsApplication",
    "icon": "ic_notification",
    "click_action": "com.example.newsapplication.MainActivity"
  },
  "data": {
    "article_url": "https://example.com/news/123",
    "category": "breaking"
  }
}
```

## 🎯 Test Different Notification Types

### 1. Simple Text Notification
```json
{
  "to": "YOUR_FCM_TOKEN",
  "notification": {
    "title": "News Update",
    "body": "Check out the latest news"
  }
}
```

### 2. Topic-Based (All Users)
```json
{
  "to": "/topics/news_updates",
  "notification": {
    "title": "Daily News",
    "body": "Today's top stories"
  }
}
```

### 3. Rich Notification with Image
```json
{
  "to": "YOUR_FCM_TOKEN",
  "notification": {
    "title": "Tech News",
    "body": "New smartphone released!",
    "image": "https://example.com/image.jpg"
  }
}
```

## ✅ Success Response
```json
{
  "multicast_id": 1234567890123456789,
  "success": 1,
  "failure": 0,
  "canonical_ids": 0,
  "results": [
    {
      "message_id": "0:1234567890abcdef"
    }
  ]
}
```

## ❌ Common Errors
- **401 Unauthorized**: Wrong server key
- **400 Bad Request**: Invalid FCM token or JSON syntax
- **200 but no notification**: Check app permissions

## 📋 Quick Testing Checklist
1. ✅ Get Server Key from Firebase Console
2. ✅ Use legacy URL: `https://fcm.googleapis.com/fcm/send`
3. ✅ Use Authorization: `key=AAAA_...`
4. ✅ Use your device FCM token above
5. ✅ Send test notification

**That's it! No OAuth2, no Node.js, no complicated setup!** 🎉