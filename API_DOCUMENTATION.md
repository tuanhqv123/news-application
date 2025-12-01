# API Documentation

This document outlines the API endpoints and schemas used by the News Application, based on the Android client implementation and the OpenAPI schema.

## Base URL
`http://10.0.2.2:8000/api/v1` (Android Emulator)
`http://127.0.0.1:8000/api/v1` (Localhost)

## Authentication

### Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "access_token": "jwt_token_here",
  "token_type": "bearer"
}
```

### Register
**POST** `/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "display_name": "User Name"
}
```

### Get Current User
**GET** `/auth/me`

**Response:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "display_name": "User Name",
  "role": "reader",
  "avatar_url": "http://..."
}
```

## Articles

### Get Articles
**GET** `/articles/`

**Query Parameters:**
- `page` (int): Page number (default: 1)
- `limit` (int): Items per page (default: 10)
- `category` (int, optional): Filter by category ID

**Response:**
```json
{
  "data": {
    "articles": [
      {
        "id": "uuid-string",
        "title": "Article Title",
        "summary": "Short summary...",
        "content": "Full content...",
        "source_url": "Source URL",
        "category_id": 1,
        "hero_image_url": "http://example.com/image.jpg",
        "created_at": "2023-01-01T12:00:00Z"
      }
    ]
  }
}
```

### Get Article Details
**GET** `/articles/{article_id}`

### Bookmark Article
**POST** `/articles/{article_id}/bookmark`

**Request Body:**
Empty JSON object `{}`.

**Response:**
- `200 OK`: Bookmark added successfully.
- `400 Bad Request`: Bookmark already exists (Duplicate key error).

### Remove Bookmark
**DELETE** `/articles/{article_id}/bookmark`

**Response:**
- `200 OK`: Bookmark removed successfully.

## Users

### Get User Bookmarks
**GET** `/users/me/bookmarks`

**Response:**
```json
{
  "results": [
    {
      "article_id": "uuid-string",
      "created_at": "...",
      "article": {
        "id": "uuid-string",
        "title": "Article Title",
        ...
      }
    }
  ]
}
```

### Update Profile
**PUT** `/auth/me`

**Request Body:**
```json
{
  "display_name": "New Name",
  "avatar_url": "http://..."
}
```

## Categories

### Get Categories
**GET** `/categories/`

**Response:**
```json
[
  {
    "id": 1,
    "name": "Technology",
    "slug": "technology"
  }
]
```

## Schemas

### Article
| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Unique identifier |
| `title` | String | Article title |
| `summary` | String | Short description |
| `content` | String | Full article content |
| `source_url` | String | Source URL |
| `category_id` | Integer | Category ID |
| `hero_image_url` | String | URL to cover image |
| `created_at` | String | ISO 8601 Date string |

### Error Response
```json
{
  "detail": [
    {
      "loc": ["body", "email"],
      "msg": "value is not a valid email address",
      "type": "value_error.email"
    }
  ]
}
```
or
```json
{
  "detail": "Error message string"
}
```
