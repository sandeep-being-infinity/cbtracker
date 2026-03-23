# CodingBat Leaderboard

A Spring Boot single-page application that displays a live leaderboard for CodingBat users with section-wise problem progress.

## Requirements

- Java 17+
- Maven 3.6+
- Internet access (to scrape codingbat.com)

## Quick Start

```bash
# From the project root directory:
mvn spring-boot:run
```

Then open your browser at: **http://localhost:8080**

## Setup

1. Edit `users.csv` in the project root directory (or via the web UI):

```csv
USERID,PROFILELINK
24251a05v0,https://codingbat.com/done?user=mukkupranitha9@gmail.com&tag=6126532500
24251a05y8,https://codingbat.com/done?user=24251a05y8@gnits.ac.in&tag=5074476821
```

2. Click **Refresh Data** in the browser to fetch progress from CodingBat.

## Features

- 📊 **Live Leaderboard** — section-wise scores for each student
- 🔄 **Async Refresh** — shows INPROGRESS → DONE status per user as data loads
- 🏷️ **Section Filter** — toggle CodingBat sections (Warmup-1, String-1, etc.) via chip UI
- 💾 **LocalStorage** — persists leaderboard data and section preferences across page reloads
- 📝 **CSV Editor** — edit users list directly from the browser

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/leaderboard` | Get current leaderboard |
| POST | `/api/refresh` | Trigger a fresh scrape |
| GET | `/api/refresh/status` | Poll refresh progress |
| GET | `/api/sections` | Get all CodingBat sections |
| GET | `/api/csv` | Get current CSV content |
| POST | `/api/csv` | Save updated CSV content |

## Build JAR

```bash
mvn package
java -jar target/codingbat-leaderboard-1.0.0.jar
```
