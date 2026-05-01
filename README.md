# 🏋️ Fitness Microservices Application

A Spring Boot microservices-based fitness tracking application that logs user activities, validates users, and generates AI-powered workout recommendations using Google Gemini.

---

## 📐 Architecture Overview

```
Client
  │
  ▼
API Gateway (port 8080)          ← OAuth2 JWT via Keycloak
  │
  ├──► User Service (port 8087)  ← PostgreSQL
  │
  ├──► Activity Service (port 8082) ← MongoDB + Kafka Producer
  │
  └──► AI Service (port 8083)    ← MongoDB + Kafka Consumer + Gemini API

Supporting Services:
  - Eureka Server (port 8761)    ← Service Discovery
  - Config Server (port 8888)    ← Centralized Configuration
  - Keycloak (port 8181)         ← Identity & Access Management
  - Apache Kafka                 ← Async Messaging
```

---

## 🧩 Services

### 1. Eureka Server
Service registry for all microservices. All services register here and discover each other by name.

### 2. Config Server
Centralized configuration server using Spring Cloud Config with native file-based storage. Serves configs for all services from `classpath:/config`.

### 3. User Service
Handles user registration and profile management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| GET | `/api/users/{id}` | Get user profile |
| GET | `/api/users/{id}/validate` | Validate user by Keycloak ID |

**Database:** PostgreSQL

### 4. Activity Service
Tracks fitness activities. Validates the user via User Service, saves the activity to MongoDB, and publishes an event to Kafka.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/activities` | Track a new activity |

**Database:** MongoDB  
**Messaging:** Kafka Producer (`activity-events` topic)

### 5. AI Service
Consumes activity events from Kafka, sends them to Google Gemini API, and stores AI-generated recommendations in MongoDB.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/recommendation/users/{userId}` | Get all recommendations for a user |
| GET | `/api/recommendation/activity/{activityId}` | Get recommendation for a specific activity |

**Database:** MongoDB  
**Messaging:** Kafka Consumer (`activity-events` topic)  
**AI:** Google Gemini API

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Language |
| Spring Boot 4.0.1 | Framework |
| Spring Cloud 2025.1.0 | Microservices tooling |
| Spring Cloud Netflix Eureka | Service discovery |
| Spring Cloud Config | Centralized configuration |
| Spring Cloud Gateway | API gateway + routing |
| Spring Security + OAuth2 | JWT authentication via Keycloak |
| Apache Kafka | Async event streaming |
| PostgreSQL | Relational DB for users |
| MongoDB Atlas | NoSQL DB for activities & recommendations |
| Google Gemini API | AI-generated fitness recommendations |
| Lombok | Boilerplate reduction |
| WebClient | Reactive HTTP client |

---

## ⚙️ Prerequisites

Make sure the following are installed and running before starting the application:

- Java 17+
- Maven 3.9+
- Docker (recommended for Kafka, PostgreSQL, Keycloak)
- MongoDB Atlas account (or local MongoDB)
- Google Gemini API key
- Keycloak instance

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/fitness-microservices.git
cd fitness-microservices
```

### 2. Set Environment Variables

The following environment variables are required:

```bash
# MongoDB
export MONGO_USER=your_mongo_username
export MONGO_PASSWORD=your_mongo_password

# Gemini AI
export GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
export GEMINI_API_KEY=your_gemini_api_key
```

### 3. Start Infrastructure Services

Start Kafka, PostgreSQL, and Keycloak. Using Docker Compose is recommended:

```bash
# Example: start Kafka
docker run -d --name kafka -p 9092:9092 apache/kafka

# Example: start PostgreSQL
docker run -d --name postgres -e POSTGRES_PASSWORD=yourpassword -p 5432:5432 postgres

# Example: start Keycloak
docker run -d --name keycloak -p 8181:8080 quay.io/keycloak/keycloak start-dev
```

### 4. Start Services in Order

Services must be started in the following order:

```bash
# 1. Eureka Server
cd eureka && ./mvnw spring-boot:run

# 2. Config Server
cd config-server && ./mvnw spring-boot:run

# 3. User Service
cd UserService && ./mvnw spring-boot:run

# 4. Activity Service
cd ActivityService && ./mvnw spring-boot:run

# 5. AI Service
cd aiservice && ./mvnw spring-boot:run

# 6. API Gateway
cd gateway && ./mvnw spring-boot:run
```

---

## 📁 Project Structure

```
fitness-microservices/
├── eureka/                  # Eureka Service Discovery Server
├── config-server/           # Spring Cloud Config Server
│   └── src/main/resources/config/
│       ├── user-service.yml
│       ├── activity-service.yml
│       ├── ai-service.yml
│       └── api-gateway.yml
├── UserService/             # User management (PostgreSQL)
├── ActivityService/         # Activity tracking (MongoDB + Kafka)
├── aiservice/               # AI recommendations (MongoDB + Kafka + Gemini)
└── gateway/                 # API Gateway (OAuth2 + JWT)
```

---

## 🔄 Data Flow

```
1. User registers via POST /api/users/register
2. Client tracks an activity via POST /api/activities
3. Activity Service validates the user against User Service
4. Activity is saved to MongoDB
5. Activity event is published to Kafka topic: activity-events
6. AI Service consumes the event from Kafka
7. AI Service sends activity data to Google Gemini API
8. Gemini returns a structured JSON recommendation
9. Recommendation is saved to MongoDB
10. Client fetches recommendations via GET /api/recommendation/users/{userId}
```

---

## 📦 Activity Request Example

```json
POST /api/activities
{
  "userId": "keycloak-user-id",
  "activityType": "RUNNING",
  "duration": 30,
  "caloriesBurned": 300,
  "startTime": "2025-01-01T07:00:00",
  "additionalMetrics": {
    "distance": "5km",
    "pace": "6min/km"
  }
}
```

### Supported Activity Types

`RUNNING`, `WALKING`, `CYCLING`, `SWIMMING`, `WEIGHT_TRAINING`, `YOGA`, `HIT`, `CARDIO`, `STRETCHING`, `OTHER`

---

## 🤖 AI Recommendation Response Example

```json
{
  "id": "...",
  "userId": "...",
  "activityId": "...",
  "type": "RUNNING",
  "recommendation": "Overall: Good session...\nPace: Slightly above average...",
  "improvements": [
    "Endurance, Consider increasing your long run distance gradually"
  ],
  "suggestion": [
    "Interval Training, Alternate between fast and slow running for 20 minutes"
  ],
  "safety": [
    "Ensure proper warm-up before running",
    "Stay hydrated throughout your session"
  ],
  "createdAt": "2025-01-01T07:35:00"
}
```

---

## 🔐 Security

Authentication is handled via **Keycloak** with JWT tokens. The API Gateway validates tokens against the Keycloak JWKS endpoint:

```
http://localhost:8181/realms/fitness-app/protocol/openid-connect/certs
```

All requests to backend services should include an `Authorization: Bearer <token>` header.

---

## ⚠️ Known Limitations

- No Docker Compose file is provided — services must be started manually
- Test classes are scaffolded but not implemented
- Passwords in config files should be moved to environment variables or a secrets manager before production use
- Error handling uses generic `RuntimeException` — custom exception types are recommended for production

---

## 📄 License

This project is for educational purposes.
