# Budakattu Sante: Project Evolution & Technical Documentation

This document provides a comprehensive overview of the development journey of Budakattu Sante, detailing the initial state, the incremental addition of features, and a deep dive into the technical stack.

---

## 1. Project Inception & Vision
**Budakattu Sante** (Tribal Market) was conceived as a digital bridge between remote tribal cooperatives and urban markets. The primary goal was to ensure fair pricing (MSP visibility), source traceability, and a professional management interface for tribal leaders, all while maintaining high accessibility for semi-literate users.

---

## 2. Technical Stack Overview

### Core Architecture
- **Pattern**: Clean Architecture with MVI (Model-View-Intent) / MVVM (Model-View-ViewModel) patterns.
- **Modularity**: Multi-module Gradle setup separating `:app`, `:data`, `:domain`, `:core:ui`, and various `:feature` modules.
- **Language**: 100% Kotlin.

### UI & UX
- **Jetpack Compose**: For a modern, declarative UI.
- **Material 3**: Utilizing the latest Material Design components.
- **Custom Theme**: "Earthy" brand palette (Copper, Stone, Forest Green) designed for cultural resonance.
- **Accessibility**: 
    - Integrated **Android Text-to-Speech (TTS)** for audio product descriptions and dashboard guidance.
    - Integrated **Speech Recognizer** for voice-to-text input supporting regional languages.

### Data & Persistence
- **Room Database**: Local SQL persistence for an **Offline-First** experience.
- **Firebase Firestore**: Real-time NoSQL cloud database for remote data synchronization.
- **Firebase Auth**: Secure authentication with support for Google Identity.
- **Firebase Storage**: Cloud storage for product images.
- **DataStore**: Preference-based storage for session management.

### Background Tasks & AI
- **WorkManager**: For reliable background data synchronization between Room and Firestore.
- **Gemini AI (via Google Generative AI SDK)**: Powering the "Tribal Sante Assistant" chatbot for multilingual user support.

### Networking & Dependency Injection
- **Hilt (Dagger)**: Standardized dependency injection across all modules.
- **ConnectivityManager**: Custom `NetworkMonitor` to detect and respond to connectivity changes.

---

## 3. Feature Evolution Roadmap

### Phase 1: Foundation & Market Exploration
- **Base Architecture**: Established the multi-module project structure.
- **Firestore Integration**: Initially implemented as a direct-to-cloud repository for Products and MSP (Minimum Support Price) records.
- **Catalog & Detail Screens**: Basic display of forest produce with high-quality imagery.

### Phase 2: Offline-First Reliability
- **Local Persistence**: Introduced **Room Database**.
- **Offline-First Repositories**: Refactored `FirestoreProductRepository` and `FirestoreMspRepository` into `OfflineFirst` implementations. Data is now served from Room, with background syncs to Firestore.
- **SyncWorker**: Implemented `WorkManager` to ensure data remains consistent even in low-network forest zones.

### Phase 3: Accessibility & Inclusive Design
- **Audio Guidance**: Added TTS support to read product "Origin Stories" and Dashboard instructions.
- **Multilingual AI Assistant**: 
    - Integrated **Gemini AI** to answer user queries.
    - Added **Voice Input** support, allowing users to speak in regional languages (Kannada, Hindi, etc.).
    - Implemented "Language Matching," where the AI responds in the same language detected from the user's voice or text.

### Phase 4: Transaction & Management Pipeline
- **Secure Payment Simulation**: Integrated a dummy **Razorpay-style gateway** to facilitate testing of the payment flow.
- **Redirection Logic**: Optimized the "Secure Payment" flow to redirect users back to the Marketplace with a success confirmation.
- **Leader Operations**: Enhanced the Leader Dashboard and Inventory management, ensuring that adding new products redirects the leader to their Inventory Warehouse.

---

## 4. How to Verify the Technical Setup

### Database Inspection
Use the **App Inspection > Database Inspector** in Android Studio to view the `budakattu-database`. You can see the `products` and `msp_records` tables being updated live.

### AI & Speech
Test the **Tribal Sante Assistant** by tapping the FAB. Use the microphone icon to speak a query in a regional language; the system uses `RecognizerIntent` for capture and Gemini for intelligent response generation.

### Background Sync
Monitor the **WorkManager Inspector** to see `SyncWorker` tasks. These tasks bridge the local Room cache with the Firebase Firestore backend, ensuring reliable data availability.

---

## 5. Summary of Tech Used
| Category | Technology |
| :--- | :--- |
| UI | Jetpack Compose, Material 3, Coil (Image Loading) |
| Architecture | Hilt, Clean Architecture, Kotlin Coroutines & Flow |
| Backend | Firebase (Firestore, Auth, Storage) |
| Local Data | Room, DataStore |
| AI / Language | Gemini AI, Android Speech API (TTS & STT) |
| Logic/Tasks | WorkManager, SavedStateHandle |
| Payments | Custom Mock Pipeline (Dummy Gateway) |

---
*Created on: May 2026 | Budakattu Sante Documentation*
