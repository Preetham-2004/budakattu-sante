# Budakattu-Sante Technical Blueprint

## Project Summary

**Budakattu-Sante** is an offline-first Android marketplace connecting tribal cooperatives to urban buyers. The system must support zero-connectivity field operations, real-time buyer updates, traceable supply chains, MSP transparency, and AI-assisted accessibility while remaining maintainable on a production codebase.

This blueprint is written for a production-grade Android implementation using Kotlin, Jetpack Compose, Material 3, MVVM, Clean Architecture, Hilt, Room, Firebase, WorkManager, Coroutines/Flow, Coil, Gemini, Navigation Compose, DataStore, and modular Gradle modules.

---

## Part 1 - System Architecture

### 1.1 Architectural Style

The app uses **Clean Architecture + MVVM + Repository Pattern**.

Dependency rule:

- `app` depends on feature modules and DI setup
- `feature:*` depends on `domain`, `core:ui`, `navigation-contracts` if introduced later
- `data` depends on `domain`
- `domain` depends on nothing Android-specific

No Android framework class may leak into `domain`.

### 1.2 Layers

#### Presentation Layer

Contains:

- Compose screens
- ViewModels
- UI models
- navigation definitions
- one-shot UI events
- screen-specific reducers or mappers

Responsibilities:

- render immutable UI state
- react to user input
- call domain use cases
- expose `StateFlow<UiState>`
- emit `SharedFlow<UiEvent>`
- remain business-rule light

Rules:

- no direct DAO calls
- no Firestore calls
- no Room imports in screens
- no pricing/MSP/stock rules in composables

#### Domain Layer

Contains:

- domain models
- repository interfaces
- use cases
- domain services
- validators
- business rules

Core domain services:

- `MspValidator`
- `StockPolicy`
- `TraceabilityAssembler`
- `SyncConflictResolver`
- `OrderValidator`
- `RoleAccessPolicy`

Responsibilities:

- define the meaning of the business
- isolate logic from implementation details
- enable pure JVM tests

#### Data Layer

Contains:

- Room entities and DAOs
- Firestore DTOs and remote data sources
- repository implementations
- mappers
- DataStore preferences
- WorkManager sync engine
- Gemini repository implementation

Responsibilities:

- local persistence
- remote IO
- conflict resolution execution
- cache orchestration
- queue processing

### 1.3 Dependency Flow

Standard flow:

`UI -> ViewModel -> UseCase -> Repository Interface -> RepositoryImpl -> Local/Remote Data Sources`

Offline write flow:

`UI -> ViewModel -> UseCase -> Repository -> Room write -> SyncQueue write -> immediate UI update`

Online sync flow:

`WorkManager -> SyncRepository -> Firestore/API -> local status update -> UI observes Room changes`

### 1.4 Scalability Strategy

Scalability is achieved by separating by **business capability**, not by technical type alone.

Recommended module layout:

- `:app`
- `:core:common`
- `:core:ui`
- `:core:network`
- `:core:database`
- `:core:testing`
- `:domain`
- `:data`
- `:feature:auth`
- `:feature:onboarding`
- `:feature:catalog`
- `:feature:productdetail`
- `:feature:preorder`
- `:feature:orders`
- `:feature:traceability`
- `:feature:leader`
- `:feature:inventory`
- `:feature:supplylog`
- `:feature:msp`
- `:feature:chatbot`
- `:feature:profile`
- `:feature:settings`
- `:navigation`

Why:

- feature ownership stays local
- parallel development is easier
- low coupling helps internship-to-production growth
- testing remains focused

### 1.5 Offline-First Architecture

Room is the **authoritative client-side source of truth**.

Rules:

- every read comes from Room first
- every create/update/delete writes to Room first
- every mutable write also creates or updates a sync queue entry
- UI never blocks on network round-trips

This is critical because tribal leaders may operate in low or zero connectivity zones for long periods.

#### Read Strategy

- UI subscribes to Room `Flow`
- repository may trigger remote refresh in background
- refreshed remote data is normalized into Room
- UI redraws automatically from local database changes

#### Write Strategy

- validate request in domain
- write locally with `syncStatus = PENDING`
- insert `SyncQueueEntity`
- schedule sync
- if online, WorkManager can run immediately
- if offline, queue persists until network returns

### 1.6 Synchronization Pipeline

1. Leader adds or edits product offline
2. Repository updates Room tables
3. Repository inserts `SyncQueueEntity`
4. `SyncScheduler` enqueues `OneTimeWorkRequest`
5. `PeriodicWorkRequest` remains as a safety net
6. Worker fetches pending queue in FIFO order
7. Worker maps payload to Firestore write model
8. Firestore upload occurs with metadata:
   - `lastModifiedAt`
   - `updatedBy`
   - `deviceId`
   - `version`
9. On success:
   - mark queue item `SYNCED`
   - clear local pending flags
   - persist remote sync metadata
10. On conflict:
   - mark item `CONFLICT`
   - preserve both local and remote snapshots for manual leader review

### 1.7 Caching Strategy

Use layered caching:

- Room for structured queryable cache
- Firestore offline cache for remote resiliency
- Coil disk/memory cache for images
- Room AI response cache for Gemini outputs
- DataStore for lightweight preferences and session flags

Cache policy by domain:

- products: Room + Firestore
- orders: Room + Firestore
- supply logs: Room + Firestore
- MSP records: Room with periodic remote refresh
- chatbot history: Room primary cache, Firestore backup sync
- images: Coil only, store URLs not blobs

### 1.8 State Management Architecture

Each screen exposes:

- `StateFlow<ScreenUiState>`
- `SharedFlow<ScreenEvent>`

Recommended UI state pattern:

```kotlin
sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Success(
        val product: ProductDetailUi,
        val relatedProducts: List<ProductCardUi>,
        val isOffline: Boolean,
        val syncBanner: SyncBannerUi?,
    ) : ProductDetailUiState
    data class Error(val message: UiText) : ProductDetailUiState
    data object Empty : ProductDetailUiState
}
```

Why:

- state survives recomposition
- events do not replay incorrectly
- lifecycle-aware collection is straightforward

### 1.9 Error Handling Architecture

Use a unified error model across layers.

Suggested sealed result:

```kotlin
sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object AuthenticationExpired : AppError
    data object PermissionDenied : AppError
    data class Validation(val reason: String) : AppError
    data class Conflict(val entityId: String) : AppError
    data class Remote(val code: Int?, val message: String?) : AppError
    data class Local(val throwable: Throwable) : AppError
}
```

Guidelines:

- domain returns typed validation failures
- repositories map Firebase/Room exceptions into `AppError`
- ViewModels translate `AppError` into user-facing `UiText`
- fatal crashes go to Crashlytics
- recoverable errors show inline retry UI

### 1.10 API Communication Strategy

Primary backend is Firebase. Retrofit is optional and used only for:

- MSP external data source if not managed in Firestore
- logistics provider integration
- payment orchestration if a custom API is introduced

Network rules:

- all remote models stay in `data.remote`
- use DTO-to-domain mapping
- apply timeouts and retry where idempotent
- keep authentication interceptors isolated in `core:network`

### 1.11 Firebase Integration Strategy

Firebase responsibilities:

- Authentication for identity
- Firestore for cloud data sync
- Storage for product images
- Crashlytics for crash monitoring
- Analytics optional
- Cloud Functions for admin workflows and claims management

Approach:

- Firestore persistence enabled
- but Room remains app-level source of truth
- custom claims store role and cooperative information
- writes are validated by Firestore rules, never by client alone

### 1.12 AI Integration Strategy

Gemini features sit behind a dedicated `AiRepository`.

Responsibilities:

- chatbot responses
- seasonal recommendation generation
- product audio description text generation
- prompt versioning
- response caching
- safety filtering

Important rule:

- Gemini never becomes a hard dependency for core commerce paths
- product browsing, ordering, inventory, and sync must work fully without AI

---

## Part 2 - Project Structure

```text
budakattu-sante/
├── app/                         # Android application entry point
├── core/
│   ├── common/                  # Result wrappers, dispatchers, constants, extensions
│   ├── ui/                      # Theme, design tokens, reusable Compose components
│   ├── network/                 # Retrofit/OkHttp, interceptors, serializers
│   ├── database/                # Shared DB builders, type converters, encryption helpers
│   └── testing/                 # Fakes, fixtures, test dispatchers, helpers
├── domain/
│   ├── model/                   # Pure business models
│   ├── repository/              # Repository contracts
│   ├── usecase/                 # Business operations
│   └── util/                    # Validators, policies, domain services
├── data/
│   ├── local/
│   │   ├── db/                  # Room database
│   │   ├── dao/                 # DAO interfaces
│   │   ├── entity/              # Room entities
│   │   └── datastore/           # DataStore preferences
│   ├── remote/
│   │   ├── firebase/            # Firestore/Auth/Storage data sources
│   │   ├── api/                 # Retrofit APIs if needed
│   │   └── ai/                  # Gemini remote client integration
│   ├── mapper/                  # Entity/DTO/domain mapping
│   ├── repository/              # Repository implementations
│   ├── sync/                    # WorkManager, sync engine, queue processors
│   └── util/                    # Serialization, conflict helpers, network monitors
├── feature/
│   ├── splash/
│   ├── onboarding/
│   ├── auth/
│   ├── catalog/
│   ├── productdetail/
│   ├── preorder/
│   ├── orders/
│   ├── traceability/
│   ├── leader/
│   ├── inventory/
│   ├── supplylog/
│   ├── msp/
│   ├── chatbot/
│   ├── profile/
│   └── settings/
├── navigation/                  # Root graphs, nested graphs, destinations
├── di/                          # Hilt modules, qualifiers, bindings
└── docs/                        # Architecture docs, ADRs, API contracts
```

### Folder Purposes

- `app`: application class, activity host, startup coordination
- `core/common`: cross-cutting primitives used everywhere
- `core/ui`: design system and reusable visual building blocks
- `core/network`: HTTP config, serializers, connectivity support
- `core/database`: shared DB construction and encryption setup
- `core/testing`: testing infrastructure reused across modules
- `domain`: business meaning and rules
- `data`: all implementation details of persistence and remote sync
- `feature/*`: independently evolvable user-facing capabilities
- `navigation`: centralized route definitions and graph composition
- `di`: production dependency graph with Hilt
- `docs`: living technical documentation

---

## Part 3 - Database Design

### 3.1 Room Entities

#### UserEntity

```kotlin
@Entity(
    tableName = "users",
    indices = [Index("role"), Index("cooperativeId")]
)
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String, // BUYER, LEADER
    val cooperativeId: String?,
    val isActive: Boolean,
    val lastLoginAt: Long?,
    val syncStatus: String,
    val lastModifiedAt: Long,
)
```

#### CategoryEntity

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val name: String,
    val displayOrder: Int,
    val iconUrl: String?,
)
```

#### TribalFamilyEntity

```kotlin
@Entity(
    tableName = "tribal_families",
    indices = [Index("cooperativeId"), Index("village")]
)
data class TribalFamilyEntity(
    @PrimaryKey val familyId: String,
    val cooperativeId: String,
    val familyName: String,
    val tribeName: String,
    val village: String,
    val district: String,
    val latitude: Double?,
    val longitude: Double?,
    val contactName: String?,
    val syncStatus: String,
    val lastModifiedAt: Long,
)
```

#### ProductEntity

```kotlin
@Entity(
    tableName = "products",
    indices = [
        Index("categoryId"),
        Index("familyId"),
        Index("cooperativeId"),
        Index("isAvailable"),
        Index("isSeasonal"),
        Index("pendingSync")
    ]
)
data class ProductEntity(
    @PrimaryKey val productId: String,
    val cooperativeId: String,
    val familyId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val pricePerUnit: Double,
    val mspPerUnit: Double?,
    val unit: String,
    val stockQty: Int,
    val reservedQty: Int,
    val isSeasonal: Boolean,
    val seasonLabel: String?,
    val imageUrls: List<String>,
    val isAvailable: Boolean,
    val pendingSync: Boolean,
    val syncStatus: String,
    val version: Long,
    val lastModifiedAt: Long,
)
```

#### ProductBatchEntity

```kotlin
@Entity(
    tableName = "product_batches",
    indices = [Index("productId"), Index("familyId"), Index("batchCode", unique = true)]
)
data class ProductBatchEntity(
    @PrimaryKey val batchId: String,
    val batchCode: String,
    val productId: String,
    val familyId: String,
    val suppliedQty: Int,
    val harvestedAt: Long?,
    val receivedAt: Long,
    val qualityGrade: String?,
    val notes: String?,
    val pendingSync: Boolean,
    val syncStatus: String,
    val lastModifiedAt: Long,
)
```

#### OrderEntity

```kotlin
@Entity(
    tableName = "orders",
    indices = [Index("buyerUid"), Index("status"), Index("createdAt")]
)
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val buyerUid: String,
    val cooperativeId: String,
    val totalAmount: Double,
    val estimatedDeliveryAt: Long?,
    val status: String,
    val paymentStatus: String,
    val deliveryAddress: String,
    val pendingSync: Boolean,
    val syncStatus: String,
    val version: Long,
    val lastModifiedAt: Long,
    val createdAt: Long,
)
```

#### OrderItemEntity

```kotlin
@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "productId", "batchId"],
    indices = [Index("productId"), Index("batchId")]
)
data class OrderItemEntity(
    val orderId: String,
    val productId: String,
    val batchId: String?,
    val quantity: Int,
    val pricePerUnit: Double,
    val mspPerUnit: Double?,
)
```

#### SupplyLogEntity

```kotlin
@Entity(
    tableName = "supply_logs",
    indices = [Index("familyId"), Index("productId"), Index("timestamp")]
)
data class SupplyLogEntity(
    @PrimaryKey val logId: String,
    val familyId: String,
    val productId: String,
    val batchId: String,
    val cooperativeId: String,
    val quantity: Int,
    val unit: String,
    val qualityNote: String?,
    val timestamp: Long,
    val createdBy: String,
    val pendingSync: Boolean,
    val syncStatus: String,
    val lastModifiedAt: Long,
)
```

#### MspRecordEntity

```kotlin
@Entity(
    tableName = "msp_records",
    primaryKeys = ["year", "categoryId"]
)
data class MspRecordEntity(
    val year: Int,
    val categoryId: String,
    val categoryName: String,
    val minimumPrice: Double,
    val sourceLabel: String,
    val effectiveFrom: Long,
    val syncedAt: Long,
)
```

#### SyncQueueEntity

```kotlin
@Entity(
    tableName = "sync_queue",
    indices = [Index("entityType"), Index("status"), Index("createdAt")]
)
data class SyncQueueEntity(
    @PrimaryKey val queueId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val status: String,
    val retryCount: Int,
    val lastError: String?,
    val localVersion: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
```

#### ChatHistoryEntity

```kotlin
@Entity(
    tableName = "chat_history",
    indices = [Index("uid"), Index("createdAt")]
)
data class ChatHistoryEntity(
    @PrimaryKey val messageId: String,
    val uid: String,
    val role: String,
    val promptHash: String?,
    val message: String,
    val languageCode: String,
    val wasServedFromCache: Boolean,
    val pendingSync: Boolean,
    val createdAt: Long,
)
```

### 3.2 Firestore Collections

```text
/users/{uid}
/cooperatives/{cooperativeId}
/cooperatives/{cooperativeId}/products/{productId}
/cooperatives/{cooperativeId}/batches/{batchId}
/tribal_families/{familyId}
/orders/{orderId}
/orders/{orderId}/items/{itemId}
/supply_logs/{logId}
/msp_records/{year}/categories/{categoryId}
/chatbot_history/{uid}/messages/{messageId}
/sync_audit/{auditId}
```

### 3.3 Relationships

- one cooperative has many leaders
- one cooperative has many products
- one tribal family can supply many product batches
- one product can have many batches
- one order has many order items
- one batch can appear in many traceability records
- one category maps to many products

### 3.4 Indexing Strategy

Room:

- index all filter-heavy columns
- index sync status columns
- index timestamps for ordering
- index foreign-key-style identifiers

Firestore:

- composite index on `(cooperativeId, isAvailable, categoryId)`
- composite index on `(cooperativeId, isSeasonal, lastModifiedAt)`
- index order queries by `(buyerUid, createdAt desc)`
- index supply logs by `(familyId, timestamp desc)`

### 3.5 Synchronization Identifiers

Every mutable record should carry:

- `id`
- `version`
- `lastModifiedAt`
- `updatedBy`
- `deviceId`
- `syncStatus`

These are mandatory for conflict handling.

### 3.6 Offline Sync Strategy

- persist payload in `SyncQueueEntity`
- payload contains a normalized JSON snapshot
- worker replays payload rather than reconstructing from current state
- this prevents accidental sync of a later mutated version under an older queue item

### 3.7 Conflict Resolution

Default strategy:

- last-write-wins using `lastModifiedAt` + `version`

Leader override:

- conflict record is shown in a dedicated resolution screen
- leader can pick:
  - keep local
  - keep server
  - merge manually

For auditability, conflict resolutions should write a `sync_audit` log in Firestore.

### 3.8 Caching Logic

- fresh writes hit Room immediately
- Firestore sync refreshes Room after success
- stale-but-available data is preferred over empty screen
- TTL-based refresh can be used for MSP and recommendations

---

## Part 4 - Application Flow

### 4.1 Buyer Flow

1. Splash
2. Local session check from DataStore/Firebase cached user
3. Onboarding for first-time users
4. Login or signup
5. Buyer home catalog
6. Category filtering and search
7. Product detail
8. Play audio description
9. Review MSP and traceability
10. Add preorder quantity
11. Validate stock
12. Confirm address and payment
13. Place preorder
14. View order tracking
15. Trace source family and batch

### 4.2 Leader Flow

1. Splash
2. Login
3. Role check from claims and cached profile
4. Leader dashboard
5. Add or edit inventory
6. Add supply log
7. Create product batch mapping
8. Work offline if needed
9. Sync pending records when network appears
10. Review conflicts
11. Monitor buyer orders
12. Review MSP warnings and inventory health

### 4.3 Important Screen Set

Mandatory screens for production roadmap:

- Splash
- Onboarding
- Login
- Signup
- Buyer Home
- Product Detail
- Cart/Preorder
- Orders
- Order Detail
- Traceability Detail
- Profile
- Settings
- Leader Dashboard
- Inventory List
- Inventory Entry/Edit
- Supply Log
- MSP Dashboard
- Sync Status
- Conflict Resolution
- Chatbot

---

## Part 5 - Navigation Architecture

### 5.1 Root Graph

```kotlin
NavHost(
    startDestination = rootStartRoute
) {
    authGraph()
    buyerGraph()
    leaderGraph()
    commonGraph()
}
```

### 5.2 Nested Graphs

- `authGraph`
  - splash
  - onboarding
  - login
  - signup
- `buyerGraph`
  - home
  - productDetail
  - preorder
  - orders
  - traceability
  - chatbot
  - profile
- `leaderGraph`
  - dashboard
  - inventory
  - supplyLog
  - msp
  - orders
  - sync
  - conflicts
  - chatbot
- `commonGraph`
  - settings
  - help
  - profile

### 5.3 Role-Based Routing

Startup routing:

- unauthenticated -> `authGraph`
- authenticated buyer -> `buyerGraph`
- authenticated leader -> `leaderGraph`

Guard strategy:

- `SessionManager` exposes current auth state
- `RoleAccessPolicy` checks permissions before route navigation
- unauthorized route attempts redirect to login or allowed home

### 5.4 Screen Access Rules

Buyer can access:

- catalog
- product detail
- preorder
- orders
- chatbot
- traceability
- profile
- settings

Leader can access:

- catalog if shared
- dashboard
- inventory entry/edit
- supply log
- sync status
- conflict resolution
- cooperative orders
- msp dashboard
- chatbot
- profile
- settings

Restricted actions:

- only leaders can create/edit products
- only leaders can create supply logs
- only buyers can place buyer preorders
- both can read traceability, but leader sees richer internal metadata

### 5.5 Deep Linking Strategy

Supported links:

- `budakattu://product/{productId}`
- `budakattu://order/{orderId}`
- `budakattu://trace/{batchId}`

Use cases:

- WhatsApp sharing by leaders
- direct buyer entry from campaigns
- support troubleshooting links

---

## Part 6 - UI/UX System

### 6.1 Visual Direction

The UI should feel:

- earthy
- premium
- warm
- modern
- rooted in tribal identity without becoming visually noisy

Use forest, bark, parchment, amber, and muted leaf tones.

### 6.2 Color Palette

```kotlin
val ForestPrimary = Color(0xFF2D5016)
val ForestDeep = Color(0xFF1F3610)
val LeafAccent = Color(0xFF7BAF45)
val BarkBrown = Color(0xFF6B3A1F)
val AmberHarvest = Color(0xFFC4884A)
val Parchment = Color(0xFFF5EDD6)
val MistGlass = Color(0xCCF8F3E8)
val StoneText = Color(0xFF2A261F)
val MspSafe = Color(0xFF3B6D11)
val MspWarning = Color(0xFFC27A10)
val MspDanger = Color(0xFFA32D2D)
```

### 6.3 Typography System

Use a refined combination:

- display/headline: serif or humanist display font
- body: clean sans-serif

Scale:

- `DisplayLarge`
- `HeadlineMedium`
- `TitleLarge`
- `BodyLarge`
- `BodyMedium`
- `LabelLarge`
- `LabelSmall`

### 6.4 Spacing System

Use 4dp base scale:

- `4, 8, 12, 16, 20, 24, 32, 40, 48`

Suggested tokens:

- `SpaceXs = 4.dp`
- `SpaceSm = 8.dp`
- `SpaceMd = 16.dp`
- `SpaceLg = 24.dp`
- `SpaceXl = 32.dp`

### 6.5 Reusable Component Strategy

Core components:

- `ForestCard`
- `ProductCard`
- `MspBadge`
- `TraceabilityTimeline`
- `SyncStatusBanner`
- `SeasonChip`
- `FamilyAttributionCard`
- `GlassSurface`
- `PrimaryCtaButton`
- `OfflineNotice`
- `QuantityStepper`
- `ShimmerPlaceholder`

### 6.6 Layout Strategy

- single-column mobile-first layouts
- tablet-ready master/detail for future expansion
- adaptive grids for category/product tiles
- avoid nested lazy layouts where possible

### 6.7 Accessibility-First Design

- large touch targets
- high contrast text against parchment surfaces
- semantic content descriptions
- TTS support for product pages
- reduced motion handling
- screen-reader-friendly field labels

### 6.8 Glassmorphism Usage

Use sparingly:

- top summary cards
- AI chatbot header
- leader sync banner

Avoid on list-heavy areas for low-end devices.

### 6.9 Animation Strategy

Allowed animations:

- fade + slide screen entry
- lazy list placement animation
- subtle button press scaling
- shimmer loading
- sync indicator pulse

Avoid:

- heavy blur-driven transitions
- long spring chains on low-end devices

### 6.10 Low-End Device Optimization

- minimize overdraw
- limit shadows and blur radius
- prefer static gradients over runtime heavy graphics
- downsample images
- disable non-essential motion for accessibility and older devices

---

## Part 7 - State Management

### 7.1 UI State Handling

Every screen exposes a single immutable UI state model.

Pattern:

- loading
- success
- error
- empty
- offline-specific variant when needed

### 7.2 StateFlow Usage

- `StateFlow` for current screen state
- `SharedFlow` for one-time events:
  - navigation
  - snackbars
  - permission prompts
  - TTS play requests

### 7.3 ViewModel Responsibilities

- call use cases
- combine flows from repositories
- map domain models to UI models
- expose screen state
- manage retry and refresh triggers
- never own Android views

### 7.4 Event Handling

Use explicit actions:

```kotlin
sealed interface ProductDetailAction {
    data object Retry : ProductDetailAction
    data object PlayAudio : ProductDetailAction
    data class QuantityChanged(val value: Int) : ProductDetailAction
    data object PlacePreorder : ProductDetailAction
}
```

### 7.5 Recomposition Optimization

- annotate UI models with `@Immutable`
- stable item keys in lists
- use `collectAsStateWithLifecycle`
- extract complex item cards
- avoid holding mutable collections in composables
- use `derivedStateOf` only when useful

---

## Part 8 - Firebase Architecture

### 8.1 Authentication Flow

Supported auth:

- email/password
- Google sign-in for buyers
- leader onboarding approval flow

Session flow:

- login through Firebase Auth
- fetch ID token
- refresh custom claims
- cache profile locally
- route by role

### 8.2 Role Management

Use Firebase custom claims:

- `role`
- `cooperativeId`
- `districtId`

Claims set by admin process or Cloud Function after approval.

### 8.3 Firestore Data Strategy

- product data partitioned by cooperative
- buyer catalog may use `collectionGroup("products")`
- write-heavy leader data kept scoped to cooperative
- immutable supply logs reduce accidental corruption

### 8.4 Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }

    match /cooperatives/{cooperativeId}/products/{productId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && request.auth.token.role == 'LEADER'
        && request.auth.token.cooperativeId == cooperativeId;
    }

    match /tribal_families/{familyId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && request.auth.token.role == 'LEADER';
    }

    match /orders/{orderId} {
      allow create: if request.auth != null
        && request.auth.token.role == 'BUYER';
      allow read: if request.auth != null
        && (
          request.auth.uid == resource.data.buyerUid ||
          request.auth.token.role == 'LEADER'
        );
    }

    match /supply_logs/{logId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.auth.token.role == 'LEADER';
      allow update, delete: if false;
    }
  }
}
```

### 8.5 Offline Persistence

Enable Firestore offline persistence, but treat it as a transport cache, not as the authoritative app model.

Why:

- Firestore cache is not ideal for all local relational queries
- Room is better for joins, traceability queries, queue operations, and deterministic offline UX

### 8.6 Scalable Querying Strategy

- use pagination for catalog
- use document snapshots for continuation
- denormalize small display fields when beneficial
- avoid N+1 family lookups by embedding lightweight preview info in product docs when justified

---

## Part 9 - WorkManager Sync Design

### 9.1 Worker Types

- `ProductSyncWorker`
- `OrderSyncWorker`
- `SupplyLogSyncWorker`
- `UserProfileSyncWorker`
- `ChatHistorySyncWorker`
- optional `FullRefreshWorker`

### 9.2 Scheduling

- immediate `OneTimeWorkRequest` after local write
- periodic safety sync every 15 minutes
- unique work names per entity type
- optional app-start reschedule

### 9.3 Constraints

- network connected
- battery not low
- storage not low if large uploads are involved

### 9.4 Retry Mechanism

- exponential backoff
- capped retry attempts
- classify errors:
  - transient -> retry
  - auth expired -> halt and request login
  - validation failure -> mark failed
  - conflict -> mark conflict

### 9.5 Queue System

Queue is persistent in Room.

Each queue item contains:

- entity type
- entity id
- operation
- payload snapshot
- retry count
- status
- last error

### 9.6 Background Pipeline

1. fetch pending queue items
2. lock item to `IN_PROGRESS`
3. push to Firestore
4. inspect remote metadata
5. resolve conflicts
6. update local entity sync flags
7. mark queue row result
8. continue until queue drained or hard failure

### 9.7 Battery Optimization

- avoid exact alarms
- batch same-entity syncs
- avoid wake-heavy polling
- use WorkManager to stay Doze-friendly

---

## Part 10 - Gemini AI Integration

### 10.1 Chatbot Architecture

Flow:

`ChatScreen -> ChatViewModel -> GetChatResponseUseCase -> AiRepository -> GeminiRemoteDataSource`

Chat components:

- prompt builder
- safety filter
- response cache
- chat history persistence
- retry policy

### 10.2 TTS Generation Flow

1. Buyer opens product detail
2. Taps audio description
3. ViewModel checks cached generated summary
4. If absent and online, Gemini generates concise description text
5. Text cached in Room
6. Android `TextToSpeech` reads generated text

Important:

- do not synthesize audio files unless needed
- generate text and let device TTS speak it
- cheaper and lighter for low-end devices

### 10.3 Prompt Management

Keep prompts versioned in code:

```kotlin
object PromptTemplates {
    const val PRODUCT_AUDIO_V1 = "..."
    const val CHAT_ASSISTANT_V1 = "..."
    const val SEASONAL_RECO_V1 = "..."
}
```

### 10.4 AI Request Optimization

- hash prompts and cache responses
- debounce chat typing submissions if needed
- stream only chatbot responses
- use non-streaming for short audio summaries and recommendations

### 10.5 Caching AI Responses

Room table:

- `AiCacheEntity(promptHash, promptVersion, response, createdAt, expiresAt)`

Use TTL:

- chatbot: short TTL or no TTL with history record
- product audio description: long TTL
- seasonal recommendations: medium TTL

### 10.6 Safety Handling

- content moderation on prompts and responses
- avoid medical/legal certainty
- avoid false product claims
- add fallback message for unsafe or unavailable output

### 10.7 Low Bandwidth Strategy

- cache aggressively
- send compact prompts
- avoid image-based model calls in MVP
- queue non-critical AI requests for later when offline

---

## Part 11 - Security Architecture

### 11.1 AES-256 Local Encryption

Use encrypted local storage for sensitive preferences and SQLCipher-backed Room for protected local data if project scope allows.

Recommended approach:

- Android Keystore-managed master key
- encrypted preferences for auth/session metadata
- encrypted DB support for sensitive local tables

### 11.2 Secure Auth Handling

- Firebase SDK handles token refresh
- never store passwords locally
- clear local session on logout
- wipe sensitive caches when user switches account

### 11.3 Firestore Security

- rules enforce role
- client role checks are convenience only
- immutable logs for sensitive supply events

### 11.4 Role Validation

- route guard in app
- use case guard in domain
- Firestore rule guard in backend

Three-layer enforcement prevents accidental privilege leaks.

### 11.5 Secure API Handling

- Gemini key injected via Gradle/build config for debug only
- production calls should ideally route through a secure backend if abuse risk is high
- never hardcode secrets in source

### 11.6 Token Management

- call `getIdToken(false)` for normal operations
- force refresh on role-sensitive refresh paths
- handle token expiration by retrying once, then re-auth flow

---

## Part 12 - Performance Optimization

### 12.1 Compose Optimization

- split large screens into focused composables
- immutable UI models
- avoid passing large unstable objects
- use lazy lists with keys
- avoid expensive recomposition in animated surfaces

### 12.2 Lazy Loading

- paginate product catalog
- load thumbnails in lists
- fetch detail-only data on product detail

### 12.3 Image Optimization

- Coil memory and disk cache enabled
- serve thumbnails and full-size URLs separately
- specify request sizes
- crossfade lightly

### 12.4 Offline Caching

- Room primary cache
- image disk cache
- AI response cache
- DataStore for lightweight state

### 12.5 Startup Optimization

- keep splash brief
- determine route from cached auth/profile
- defer non-critical initialization
- initialize expensive AI clients lazily

### 12.6 Memory Optimization

- avoid storing bitmaps in ViewModels
- clear TTS and media resources
- keep lists paged
- avoid huge JSON payload duplication in memory

### 12.7 Low RAM Device Optimization

- disable expensive blur
- reduce simultaneous image prefetch
- keep item layouts shallow
- prefer text-first placeholders over image-heavy skeletons

---

## Part 13 - Testing Strategy

### 13.1 Unit Testing

Test pure domain logic:

- `MspValidatorTest`
- `StockPolicyTest`
- `OrderValidatorTest`
- `RoleAccessPolicyTest`
- `SyncConflictResolverTest`

### 13.2 ViewModel Testing

Use:

- `kotlinx-coroutines-test`
- fake repositories
- Turbine for flow assertions if added

Test:

- loading state
- success state
- offline fallback
- event emission
- retry behavior

### 13.3 Repository Testing

Test:

- local-first reads
- write queues sync entries
- Firestore sync updates local state
- conflict detection behavior

### 13.4 UI Testing

Compose UI tests:

- product cards render images and MSP badge
- preorder button disabled when stock invalid
- leader forms save offline
- navigation to detail works

### 13.5 Offline Sync Testing

Test cases:

- create product while offline
- app restart before sync
- reconnect and sync resumes
- conflict item surfaces correctly

### 13.6 Firebase Testing

Use Firebase Emulator Suite for:

- auth flow
- Firestore rules
- role checks
- write/read authorization

### 13.7 WorkManager Testing

Use:

- WorkManager test helpers
- fake sync repository

Validate:

- retry on transient network error
- failure on validation error
- success marks queue synced

---

## Part 14 - Scalability Plan

### 14.1 Multiple Districts

- district becomes metadata, not code branching
- cooperative scoping handles data partitioning
- UI filters by district when needed

### 14.2 Thousands of Products

- Firestore pagination
- Room paging integration later
- thumbnail-first image strategy
- list filtering locally where sensible

### 14.3 Many Cooperatives

- cooperative partitioned collections
- collection group query for marketplace view
- custom claims for scoped leader access

### 14.4 Multilingual Support

- all strings in resources
- localized DataStore preference
- prompt language parameter for Gemini
- TTS language fallback chain

### 14.5 Future Logistics Integration

Introduce:

- `LogisticsRepository`
- tracking sync worker
- delivery provider API module

No rewrite needed if this stays behind domain contracts.

---

## Part 15 - Development Roadmap

### Phase 1 - Core App Foundation

Priority:

1. Clean Architecture module setup
2. Hilt setup
3. Navigation graphs
4. Splash, onboarding, auth, home shell
5. product detail screen

Complexity: medium

Deliverables:

- role-based routing
- base design system
- placeholder feature shells

### Phase 2 - Database + Offline First

Priority:

1. full Room schema
2. DAOs and repositories
3. offline inventory flows
4. sync queue persistence
5. local-first catalog

Complexity: high

Deliverables:

- usable offline buyer catalog
- leader local inventory entry
- queue-backed writes

### Phase 3 - Firebase Backend

Priority:

1. Firebase Auth
2. Firestore product sync
3. order sync
4. user profile sync
5. Storage image uploads

Complexity: high

Deliverables:

- authenticated app
- cloud-synced catalog
- role-based access

### Phase 4 - Business Logic

Priority:

1. preorder validation
2. stock engine
3. traceability
4. MSP enforcement

Complexity: high

Deliverables:

- production-worthy commerce logic
- resume-strong traceability feature

### Phase 5 - AI Features

Priority:

1. chatbot
2. TTS descriptions
3. seasonal recommendations

Complexity: medium

Deliverables:

- accessibility and engagement layer

### Phase 6 - UI/UX Improvement

Priority:

1. refined cards
2. animations
3. shimmer and empty states
4. dark mode and polish

Complexity: medium

### Phase 7 - Leader Features

Priority:

1. leader dashboard
2. supply log
3. inventory management
4. sync monitor

Complexity: high

### Phase 8 - Production Features

Priority:

1. security hardening
2. global error handling
3. performance optimization
4. telemetry

Complexity: medium to high

### Phase 9 - Final Polish

Priority:

1. testing completion
2. release config
3. app icon and branding
4. signed build
5. store-readiness checklist

Complexity: medium

---

## Recommended Exact Build Order

1. Architecture setup
2. Hilt dependency graph
3. Navigation system
4. Room schema
5. Firebase Auth
6. Firestore sync
7. Product detail screen
8. Preorder system
9. Offline inventory
10. WorkManager sync
11. Traceability
12. MSP system
13. Gemini chatbot
14. Audio descriptions
15. UI polish
16. Testing and release readiness

---

## Implementation Notes For This Repository

The current repo already contains an initial modular baseline:

- `app`
- `core/ui`
- `domain`
- `data`
- `feature/catalog`
- `navigation`

Before feature expansion, the next code changes should be:

1. add Hilt and move manual dependency wiring into `di/`
2. add `feature/auth`, `feature/productdetail`, `feature/preorder`, and `feature/leader`
3. expand Room schema beyond products and sync queue
4. implement Firebase Auth and Firestore repository paths
5. replace placeholder product detail with the production screen defined in this blueprint

This sequence keeps the current codebase aligned with the required production direction without rewriting the foundation twice.
