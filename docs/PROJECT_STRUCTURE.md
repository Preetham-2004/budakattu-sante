# Budakattu Sante: Project Structure & File Organization

This document outlines the directory structure and file organization of the Budakattu Sante project, explaining the purpose of each module and key files within the clean architecture.

---

## 1. Project Root
The root directory contains project-wide configuration, build scripts, and multi-module management.

- `build.gradle.kts`: Top-level build script.
- `settings.gradle.kts`: Defines all sub-projects/modules.
- `gradle.properties`: Project-wide properties (Hilt, Kotlin versions).
- `gradle/libs.versions.toml`: Centralized dependency management using Version Catalogs.
- `firebase/`: Contains Firebase-related security rules and configuration.
- `docs/`: Technical documentation and evolution records.

---

## 2. Module Breakdown

### 📂 :app
The entry point of the Android application.
- `src/main/java/com/budakattu/sante/`
    - `BudakattuApp.kt`: Application class, initializes Hilt.
    - `MainActivity.kt`: The single activity, hosts the main Compose UI and schedules background tasks (`SyncWorker`).
    - `di/`: Centralized Hilt modules (`DataModule`, `RepositoryModule`) for providing dependencies across all modules.

### 📂 :domain
The "Pure Kotlin" core containing business logic. No Android dependencies.
- `model/`: Data classes used throughout the app (`Product`, `MspRecord`, `User`).
- `repository/`: Interfaces defining data operations (Contract between domain and data).
- `usecase/`: Granular business logic components (e.g., `GetProductsUseCase`, `CheckoutSingleItemUseCase`).

### 📂 :data
The implementation of the domain's repository interfaces. Handles all data sources.
- `local/`: Room database implementation.
    - `db/`: `BudakattuDatabase`.
    - `dao/`: Room DAOs for products and MSP records.
    - `entity/`: Room database entities and mappers.
    - `converter/`: Room TypeConverters for complex types.
- `remote/`: Firebase implementations.
    - `firebase/`: Firestore and Firebase Auth logic.
- `repository/`: Offline-first repository implementations (orchestrates Room and Firestore).
- `sync/`: `SyncWorker` implementation for background data consistency.
- `datastore/`: Preference management for user sessions.

### 📂 :core:ui
Shared UI logic and design system.
- `theme/`: Earthy-themed Material 3 design system (`Color.kt`, `Theme.kt`, `Type.kt`).
- `components/`: Reusable Compose components (`HeritageScaffold`, `AudioGuidanceButton`, `ForestCard`, `SupportChat`).

### 📂 :navigation
Centralized navigation management.
- `NavRoutes.kt`: String constants defining all screen routes.
- `BudakattuNavHost.kt`: The single `NavHost` defining the app's navigation graph (Auth, Buyer, and Leader flows).

### 📂 :feature:*
Self-contained modules representing specific user-facing features. Each usually follows the pattern of `Screen.kt` + `ViewModel.kt`.

- **:feature:auth**: Onboarding, Login, and Signup flows.
- **:feature:catalog**: The main Marketplace/Product grid and AI Assistant entry.
- **:feature:productdetail**: Detailed product view, Audio stories, and Payment gateway integration.
- **:feature:leader**: Management dashboard, Inventory Warehouse, and Product Entry forms.
- **:feature:orders**: Buyer order history and Leader order logs.

---

## 3. Key Technical Files Summary

| File | Purpose |
| :--- | :--- |
| `BudakattuDatabase.kt` | Root of the Room local persistence. |
| `OfflineFirstProductRepository.kt` | Bridge between local Room and remote Firestore. |
| `SyncWorker.kt` | Background sync logic for low-network environments. |
| `BudakattuNavHost.kt` | The "brain" of app navigation and redirection. |
| `SupportChat.kt` | Shared AI Assistant UI for help and voice input. |
| `DummyPaymentDialog.kt` | Simulated secure payment pipeline UI. |

---
*Last Updated: May 2026 | Budakattu Sante Architecture*
