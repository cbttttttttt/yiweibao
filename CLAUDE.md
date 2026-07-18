# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**易维保智能管理App** — an Android app for equipment maintenance management in small-to-medium manufacturing enterprises. This is a **demo-stage project** implementing core functionality from the PC-based "行健易维保智能管理系统4.0" as a mobile app.

The full project proposal is at `易维保智能管理app-项目方案初稿.md`. The revised proposal (V0.2, July 2026) defines three functional layers — only Layer 1 is in scope for the current demo.

## Current State

Demo implementation is complete:

| Component | Status | Technology |
|-----------|--------|------------|
| Backend | Compiled & running | Spring Boot 3.3.5 + Java 21 + MySQL 9.3 |
| Android App | Code complete, needs Android Studio to compile | Kotlin 2.0 + Jetpack Compose + Material 3 |
| Demo data | 18 equipment, 4 users, 11 work orders | Auto-loaded via `DataInitializer.java` |

**In-scope (Layer 1):** Equipment ledger CRUD, work order lifecycle (create → accept → repair → complete), fault statistics dashboard, user login with JWT.

**Out-of-scope:** AI diagnostics, IoT sensor monitoring, knowledge graphs, predictive maintenance, spare parts, inspection/patrol, maintenance planning. These are "extension features" per the proposal.

## Project Structure

```
backend/                     # Spring Boot REST API server
  pom.xml                    # Maven config (Spring Boot 3.3.5, JPA, Security, jjwt, zxing)
  src/main/java/com/yiweibao/
    YiweibaoApplication.java
    entity/                   # User, Equipment, WorkOrder (JPA entities)
    dto/                      # ApiResponse<T>, request/response DTOs
    repository/               # Spring Data JPA repositories
    service/                  # Business logic: Auth, Equipment, WorkOrder, Statistics
    controller/               # REST controllers
    security/                 # JWT token util + auth filter
    config/                   # SecurityConfig, CorsConfig, DataInitializer, GlobalExceptionHandler

android/                     # Android Kotlin/Compose app
  app/src/main/java/com/yiweibao/app/
    MainActivity.kt          # Single-Activity entry point
    YiweibaoApp.kt           # Application class (initializes TokenManager + Retrofit)
    navigation/NavGraph.kt   # All routes: login → main(3-tab bottom nav)
    data/
      api/ApiService.kt      # Retrofit interface (15 endpoints)
      api/RetrofitClient.kt  # OkHttp client with auth interceptor
      model/Models.kt        # All data classes
      repository/            # Auth, Equipment, WorkOrder, Statistics repositories
    ui/
      login/                 # LoginScreen + LoginViewModel
      equipment/             # EquipmentList, EquipmentDetail, EquipmentForm (+ ViewModel)
      workorder/             # WorkOrderList, WorkOrderDetail, CreateWorkOrder, Repair (+ ViewModel)
      statistics/            # StatisticsScreen (+ ViewModel)
    util/TokenManager.kt     # DataStore-based JWT persistence
```

## Build & Run

### Backend

```bash
# Start MySQL first (run once)
"路径\to\mysqld" --console

# Create database (once)
mysql -u root -p<password> -e "CREATE DATABASE IF NOT EXISTS yiweibao_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

# Build
cd backend
mvn compile

# Run (auto-creates tables + seeds demo data on first start)
mvn spring-boot:run
# Or: java -jar target/yiweibao-backend-1.0.0-SNAPSHOT.jar
```

Server starts on `http://localhost:8080`. Seed data is loaded when the `users` table is empty. Demo accounts: `admin` / `123456` (manager), `engineer1` / `123456` (maintenance), `operator1` / `123456` (operator).

### Android

Open the `android/` directory in Android Studio. Sync Gradle, then run on an emulator (API 26+). The app connects to `http://10.0.2.2:8080/` (Android emulator's alias for host localhost).

## API Design

All endpoints return `{ "code": 200, "message": "success", "data": {...} }`.

Auth endpoints (`/api/auth/**`) are public; all others require `Authorization: Bearer <token>`.

Key endpoints:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login, returns JWT + user info |
| GET | `/api/equipment?keyword=&page=&size=` | Equipment list with search |
| GET/POST/PUT | `/api/equipment[/{id}]` | Equipment CRUD |
| GET | `/api/work-orders?status=0&page=&size=` | Work orders (status: 0/1/2) |
| POST | `/api/work-orders` | Create work order |
| PUT | `/api/work-orders/{id}/accept` | Accept order (sets engineer name) |
| PUT | `/api/work-orders/{id}/complete` | Complete with diagnosis + repair action |
| POST | `/api/upload` | Upload image file |
| GET | `/api/statistics/overview` | Fault summary stats |
| GET | `/api/statistics/fault-types` | Fault category distribution |
| GET | `/api/statistics/top-equipment?limit=5` | Equipment fault ranking |

## Key Constraints

- **All data is simulated** — no real machine tools or IoT sensors. Seed data in `DataInitializer.java`.
- **Chinese character support**: `application.yml` sets `characterEncoding=utf-8` and `serverTimezone=Asia/Shanghai`.
- **File uploads** stored in `./uploads/` relative to the working directory. QR codes generated in `./uploads/qrcodes/`.
- **Photo capture** is declared in AndroidManifest but the CameraX implementation is minimal — the demo currently uses text-based QR display.
- **No offline mode** — the app requires network connectivity to the backend.
