# Nexus — Real-Time Collaborative Project Management

### Android Engineering Assignment · Senior Level (8+ Years Experience)

---

## Table of Contents

1. [Overview](#overview)
2. [App Concept](#app-concept)
3. [User Stories](#user-stories)
4. [Technical Architecture](#technical-architecture)
5. [Library Requirements](#library-requirements)
6. [Key Technical Challenges](#key-technical-challenges)
7. [Local Database Schema](#local-database-schema-room)
8. [API Contract](#api-contract)
9. [UI/UX Requirements](#uiux-requirements)
10. [Testing Requirements](#testing-requirements)
11. [Performance Requirements](#performance-requirements)
12. [Security Requirements](#security-requirements)
13. [Build & CI/CD Requirements](#build--cicd-requirements)
14. [Evaluation Criteria](#evaluation-criteria)
15. [Submission Guidelines](#submission-guidelines)
16. [Bonus Challenges](#bonus-challenges)

---

## Overview

Build **Nexus**, a production-quality Android application for real-time collaborative project management. The app must function as a polished, shippable product — not a proof-of-concept. Every feature must be implemented end-to-end, including error states, loading states, empty states, and offline behaviour.

This assignment evaluates:

- **Architectural thinking** at module and system level
- **Modern Android Jetpack** library mastery
- **Real-time & offline-first** engineering
- **Performance, security, and testability**
- **Code quality** and production readiness

> The backend REST + WebSocket service is provided for you. See [API Contract](#api-contract) for the full specification. A deployable Node.js backend starter is in `/backend` — run it locally or host it on your server.

---

## App Concept

Nexus is a Kanban-style project management app where users collaborate in real time across **Workspaces → Projects → Boards → Tasks**. Multiple users see live updates as tasks are created, moved, commented on, and resolved. The app functions fully offline and syncs intelligently when connectivity is restored — all without the user ever triggering a manual refresh.

Think of it as a focused, high-quality intersection of **Linear** (task UX) and **Notion** (rich content), built natively for Android.

---

## User Stories

### Authentication & Session
- [ ] Register with email and password
- [ ] Sign in with Google (via Google Identity Services)
- [ ] Enable biometric unlock (fingerprint / face) after first successful login
- [ ] Session persists across cold starts via encrypted token storage
- [ ] Automatic silent token refresh; user is redirected to login only when refresh token expires
- [ ] Force logout from all devices via Settings

### Workspaces
- [ ] Create and switch between multiple workspaces
- [ ] Invite members by email; they receive an email with a deep-link invite
- [ ] Join a workspace via an invite deep link or QR code
- [ ] View members list with roles (Owner, Admin, Member)
- [ ] Promote/demote or remove members (Owner/Admin only)

### Projects
- [ ] Create projects with name, description, accent colour, and optional due date
- [ ] View all projects in a workspace in a card grid
- [ ] Archive and soft-delete projects
- [ ] Each project has one or more **Boards** (columns) — default boards are `To Do`, `In Progress`, `In Review`, `Done`

### Task Board
- [ ] View tasks grouped by board in a horizontal Kanban layout
- [ ] Drag and drop tasks between boards; position persists on the server
- [ ] Create tasks with: title, rich-text description, priority (None / Low / Medium / High / Urgent), assignee, labels, and due date
- [ ] Inline quick-create task at the bottom of each board column
- [ ] Long-press to multi-select tasks; bulk move, label, assign, or delete
- [ ] Real-time board updates: when a teammate moves or edits a task, it reflects immediately without manual refresh

### Task Detail
- [ ] Full task detail screen with rich-text description editor (bold, italic, strikethrough, inline code, ordered & unordered lists)
- [ ] Subtask checklist with completion tracking
- [ ] File attachments: pick from gallery, capture with camera, or scan a document with ML Kit
- [ ] Comment thread with `@mention` support (type `@` to open member picker)
- [ ] Full activity log (who changed what and when)
- [ ] Task relationships: block / is-blocked-by links to other tasks

### Notifications
- [ ] Push notifications (FCM) for: task assigned, @mentioned, comment added, due date reminder (24h before)
- [ ] In-app notification centre showing all notifications
- [ ] Mark individual or all notifications as read
- [ ] Notification preferences screen per workspace
- [ ] Tapping a push notification deep-links directly to the relevant task

### Search
- [ ] Global search across tasks, projects, and members
- [ ] Filter results by: type, workspace, assignee, priority, label, due date range
- [ ] Recent searches persisted locally
- [ ] Search results paginated with infinite scroll

### Analytics Dashboard
- [ ] Tasks completed per day (bar chart, last 30 days)
- [ ] Task distribution by priority (pie chart)
- [ ] Team velocity: tasks completed this week vs last week
- [ ] Overdue task count with tap-to-view list
- [ ] Per-member contribution breakdown
- [ ] All charts must use live data from the API (no hardcoded values)

### Offline & Sync
- [ ] All previously loaded content is readable while offline
- [ ] Create and edit tasks offline; changes enqueue and sync automatically on reconnect
- [ ] A persistent offline banner appears when there is no network
- [ ] Sync conflicts are resolved with server-wins logic; user is notified via snackbar with option to view diff
- [ ] Pending (unsynced) tasks are visually flagged with an indicator

### Profile & Settings
- [ ] Update display name, profile photo (camera or gallery), and timezone
- [ ] Light / Dark / System theme toggle
- [ ] App-wide font size preference (persisted in DataStore)
- [ ] Notification preferences per workspace
- [ ] Export personal data (JSON download)
- [ ] Account deletion

---

## Technical Architecture

### Multi-Module Structure

The project **must** use a multi-module Gradle setup. Every module must have a single, clear responsibility. Cross-module dependencies must flow **downward only** (feature modules depend on core modules, never on each other).

```
:app
│   Application entry point, Hilt component, MainActivity, top-level nav graph
│
├── :build-logic                         Convention plugins (no buildSrc)
│
├── :core
│   ├── :core:common                     Kotlin extensions, Result<T>, dispatchers, base classes
│   ├── :core:network                    Retrofit, OkHttp, WebSocket client, auth interceptor
│   ├── :core:database                   Room DB, all DAOs, entities, type converters, migrations
│   ├── :core:datastore                  Proto DataStore schemas (UserPreferences, SessionData)
│   ├── :core:ui                         Design system: theme, Material 3 tokens, shared composables
│   └── :core:testing                    Fakes, fixtures, test dispatchers, shared test rules
│
└── :feature
    ├── :feature:auth                    Login, register, biometric prompt
    ├── :feature:workspace               Workspace list, creation, member management
    ├── :feature:board                   Kanban board, drag-and-drop, quick-create
    ├── :feature:task-detail             Task detail, subtasks, comments, attachments, activity
    ├── :feature:search                  Search screen, filters, recent history
    ├── :feature:notifications           Notification centre, FCM handler
    ├── :feature:analytics               Dashboard, charts
    └── :feature:profile                 Profile editor, settings, preferences
```

### Architecture Pattern

**Clean Architecture + MVVM** with strict Unidirectional Data Flow (UDF).

```
┌─────────────────────────────────────────────┐
│                  UI Layer                   │
│   Composable → ViewModel → UiState (Flow)   │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│     UseCase (single invoke fun) + Models    │
├─────────────────────────────────────────────┤
│                Data Layer                   │
│  RepositoryImpl → RemoteDataSource          │
│               → LocalDataSource (Room)      │
└─────────────────────────────────────────────┘
```

**Hard rules:**
- ViewModels import nothing from `android.*` except `ViewModel` and `SavedStateHandle`
- Use cases have exactly one public `operator fun invoke(...)` — no utility methods
- Repository implementations own the caching strategy; callers never decide whether to go to network or cache
- No business logic inside `@Composable` functions — only state consumption and event emission

### State Modelling

Every screen defines a sealed `UiState` or a data class with exhaustive state fields:

```kotlin
data class BoardUiState(
    val columns: ImmutableList<BoardColumn> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val isOffline: Boolean = false,
    val syncPending: Int = 0               // count of unsynced local writes
)

sealed interface UiError {
    data class Message(val text: String) : UiError
    data object NetworkUnavailable : UiError
    data object SessionExpired : UiError
}
```

One-time effects (navigation, snackbars, toasts) are delivered via `Channel<UiEffect>` exposed as `Flow<UiEffect>` — **never** via `StateFlow`.

### Data Flow — Offline-First

```
Network response
      │
      ▼
Repository ──► upsert ──► Room (single source of truth)
                               │
                               ▼
                         DAO Flow<List<T>>
                               │
                               ▼
                         ViewModel StateFlow
                               │
                               ▼
                           Compose UI
```

The UI layer **never** consumes network responses directly.

---

## Library Requirements

All libraries below are mandatory unless marked **[optional]**.

### Core & DI

| Library | Version | Purpose |
|---|---|---|
| Hilt | 2.51+ | Dependency injection throughout all modules |
| Hilt Navigation Compose | 1.2+ | `hiltViewModel()` in nav graph |
| AndroidX App Startup | 1.1+ | Ordered initialiser chain (Timber, analytics) |

### UI

| Library | Purpose |
|---|---|
| Jetpack Compose BOM (latest stable) | Entire UI layer — no XML layouts |
| Material 3 | Design system, dynamic colour (Android 12+) |
| Navigation Compose | Type-safe nav graph, deep links |
| Compose Window Size Classes | Adaptive layouts for tablets and foldables |
| Coil 3 | Async image loading with disk + memory cache |
| Vico | `CartesianChartView` / Compose charts for analytics |
| Accompanist Permissions **[optional]** | Runtime permission flows |

### Networking

| Library | Purpose |
|---|---|
| Retrofit 2 | REST client with suspend fun support |
| OkHttp 4 | HTTP client; `HttpLoggingInterceptor` (debug only) |
| OkHttp WebSocket | Persistent WebSocket for real-time events |
| `kotlinx.serialization` | JSON serialisation (no Gson/Moshi) |

### Local Persistence

| Library | Purpose |
|---|---|
| Room 2.6+ | Local SQLite database; all queries return `Flow` |
| DataStore (Proto) | `UserPreferences` proto for theme, font, notifications |
| Security-Crypto (`EncryptedSharedPreferences`) | Encrypted JWT storage backed by Android Keystore |

### Async & Reactive

| Library | Purpose |
|---|---|
| Kotlin Coroutines | Structured concurrency everywhere |
| Kotlin Flow / StateFlow / SharedFlow | Reactive streams; no RxJava |
| Paging 3 | `RemoteMediator` for paginated task list and search |

### Background Work

| Library | Purpose |
|---|---|
| WorkManager 2.9+ | Offline sync queue, periodic refresh, notification scheduling |

### Camera & Media

| Library | Purpose |
|---|---|
| CameraX | In-app camera capture for attachments and profile photo |
| ML Kit Document Scanner | Scan documents as PDF/JPEG attachments **[optional bonus]** |
| Android Photo Picker | Gallery access (respects Android 13+ scoped storage) |

### Security

| Library | Purpose |
|---|---|
| BiometricPrompt | Biometric / device-credential authentication |
| OkHttp `CertificatePinner` | TLS certificate pinning on all API hosts |

### Firebase

| Library | Purpose |
|---|---|
| Firebase Cloud Messaging | Push notifications (data + notification payloads) |
| Firebase Analytics | User journey tracking |
| Firebase Crashlytics | Crash and ANR reporting |

### Testing

| Library | Purpose |
|---|---|
| JUnit 4 | Unit test runner |
| MockK | Kotlin-idiomatic mocking |
| Turbine | `Flow` / `StateFlow` assertion |
| Hilt Testing (`hiltAndroidTest`) | DI graph in instrumented tests |
| Compose UI Testing | `createComposeRule`, semantic matchers |
| OkHttp `MockWebServer` | Deterministic network layer tests |
| Robolectric | Run Android tests on JVM |
| Truth | Fluent assertions |

### Performance

| Library | Purpose |
|---|---|
| Macrobenchmark | Cold start and scroll benchmarks |
| Baseline Profiles | AOT compilation hints for critical paths |
| LeakCanary **[debug only]** | Memory leak detection |

---

## Key Technical Challenges

These are the areas where depth of implementation will be assessed most rigorously.

---

### 1. Offline-First Sync Architecture

**Requirement:** The app must be fully usable offline. The UI always reads from Room. The network is a write-through cache only.

**Implementation requirements:**

- `ConnectivityObserver` — wrap `ConnectivityManager.NetworkCallback` in a `Flow<NetworkStatus>` singleton. Every screen observes this to show the offline banner.
- **Read path:** Repository returns `Flow<T>` from Room DAO. On first load (or stale threshold exceeded), it triggers a background network fetch that upserts results into Room — the UI update arrives automatically via the Flow.
- **Write path — online:** Write to Room immediately (`SyncStatus.SYNCED`), then send to API. On API failure, mark as `SyncStatus.PENDING` and enqueue a `SyncWorker`.
- **Write path — offline:** Write to Room with `SyncStatus.PENDING`. Enqueue a `SyncWorker` with `setRequiredNetworkType(NetworkType.CONNECTED)` and exponential backoff.
- **SyncWorker:** Processes the `sync_queue` table in order. On success, removes the entry and updates the entity's `syncStatus` to `SYNCED`. On failure after max retries, marks as `SyncStatus.FAILED` and notifies the user.
- **Conflict resolution:** When the server returns an entity with a newer `updatedAt` than the locally-pending version, apply server-wins. Show a snackbar: *"Your offline edits to [Task] were overridden by a newer server version."*

---

### 2. WebSocket Real-Time Engine

**Requirement:** Board and task-detail screens must reflect remote changes with zero user interaction.

**Implementation requirements:**

- A `WebSocketManager` Hilt singleton (`@SingletonComponent`) manages a single authenticated `OkHttpClient.newWebSocket()` connection.
- **Reconnection strategy:** Exponential backoff — 1 s → 2 s → 4 s → 8 s → … → 64 s cap. Reset to 1 s after a successful ping/pong round-trip. Use `CoroutineScope(SupervisorJob() + Dispatchers.IO)`.
- **Channel subscriptions:** On board screen entry, send `{ "action": "subscribe", "channel": "project:<id>" }`. On `onStop`, unsubscribe. Manage subscription reference count so the connection is not torn down while any subscriber is active.
- **Event dispatch:** Parse incoming JSON into a `WebSocketEvent` sealed class. Dispatch to `MutableSharedFlow<WebSocketEvent>` that repositories collect. They upsert the payload into Room; the UI updates automatically through the DAO Flow chain.
- **Heartbeat:** Send `{ "action": "ping" }` every 30 s with a coroutine. If no `pong` within 10 s, force-close and reconnect.

---

### 3. Optimistic UI Updates with Rollback

**Requirement:** Drag-and-drop task moves and comment posts must feel instant.

**Flow:**
```
User action
    │
    ├─► Write new state to Room with SyncStatus.OPTIMISTIC
    │         │
    │         └─► UI updates immediately (via DAO Flow)
    │
    └─► API call (async)
            │
            ├─ Success ─► Update Room entry: SyncStatus.SYNCED
            └─ Failure ─► Revert Room to previous snapshot ─► Show error snackbar
```

Store the **pre-action snapshot** in memory (in the ViewModel or a dedicated `OptimisticCache`) before writing to Room. On failure, restore it.

---

### 4. Drag-and-Drop Kanban Board

**Requirement:** A smooth, native-feeling Kanban board in Jetpack Compose.

- The board is a `LazyRow` of `LazyColumn`s (one per board column).
- Implement a `DragDropState` that tracks the dragged item's visual offset via `Modifier.pointerInput`.
- During drag, render a floating shadow copy of the card at the pointer position using `Popup` or `Box` with `zIndex`.
- On drop: determine the target column and insertion index by hit-testing composable bounds. Write the move to Room optimistically.
- Ensure 60 fps during drag: the drag offset must be driven by a `MutableState<Offset>` updated in the `pointerInput` coroutine — **not** recomposition-triggered state.

---

### 5. Paging 3 with RemoteMediator

**Requirement:** Task list and search results use Paging 3 with Room-backed `RemoteMediator`.

```kotlin
class TaskRemoteMediator(
    private val boardId: String,
    private val api: NexusApi,
    private val db: NexusDatabase
) : RemoteMediator<Int, TaskEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TaskEntity>
    ): MediatorResult { ... }
}
```

- Maintain a `remote_keys` table keyed by `(entity = "task", boardId)` storing `prevKey` and `nextKey`.
- Handle all three `LoadType` cases: `REFRESH`, `PREPEND`, `APPEND`.
- On `REFRESH`: clear the existing page data for the board and restart from page 1.
- Expose three distinct `LoadState` UI representations: shimmer skeleton (initial), footer spinner (append), full-screen error with retry (failure).

---

### 6. Rich Text Editor

**Requirement:** Task descriptions support bold, italic, strikethrough, inline code, ordered lists, and unordered lists.

- Build on `BasicTextField` with `AnnotatedString` and a toolbar of style toggles.
- Persist the rich-text model as a JSON document (define your own lightweight schema or use a Markdown string — document your choice).
- Render rich text in read-only mode using `Text(annotatedString)` with spans mapped from your schema.
- The editor must correctly handle toggling styles on selected text ranges and merging overlapping spans.

---

### 7. Background Notification Handling (FCM + WorkManager)

**Requirement:** The app handles all FCM message types correctly in every app state.

| App state | Expected behaviour |
|---|---|
| Foreground | Parse data payload; show in-app overlay composable (slide-in card at top) |
| Background | System tray notification; tap deep-links to the correct screen |
| Killed | System tray notification; cold start at deep-link destination |

- Use **data-only FCM messages** (no `notification` key). Your `FirebaseMessagingService` handles all display logic.
- Define notification channels: `TASK_UPDATES` (default importance), `MENTIONS` (high importance), `REMINDERS` (default importance).
- Due-date reminders use a `WorkManager` `OneTimeWorkRequest` scheduled with `setInitialDelay`.
- Deep-link URI pattern: `nexus://task/{taskId}` → navigates to `:feature:task-detail`
- Register the deep link in `AndroidManifest.xml` and in the Navigation Compose graph with `<deepLink>`.

---

### 8. Biometric + Encrypted Session

**Requirement:** Auth tokens are stored encrypted; biometric unlock is offered on app resume.

- **Storage:** Use `EncryptedSharedPreferences` with a `MasterKey` backed by Android Keystore. Store `accessToken` and `refreshToken` only here — never in plain `SharedPreferences` or `DataStore`.
- **Biometric gate:** After the app is backgrounded for > 5 minutes, show a `BiometricPrompt` on resume before showing any content.
- Implement `BiometricManager.canAuthenticate()` gating — fall back to device PIN/pattern if biometric hardware is unavailable.
- Lock screen overlay: while the biometric prompt is pending, obscure the content with a full-screen composable so the recent-apps thumbnail is also protected (`FLAG_SECURE`).

---

### 9. Performance — Startup & Rendering

**Requirement:** Measurable performance targets enforced by Macrobenchmark tests.

| Metric | Target |
|---|---|
| Cold start → first frame | < 500 ms |
| Board render (50 tasks) | < 300 ms |
| Task list fling (RecyclerView / LazyColumn) | 0 dropped frames at 60 fps |
| Release APK size | < 25 MB |

- Create a `:benchmark` Gradle module with `MacrobenchmarkRule` tests for:
  - `startupBenchmark` — measures cold start time
  - `boardScrollBenchmark` — measures `frameDuration` during a fling on the board
- Generate Baseline Profiles: add `BaselineProfileRule` test in `:benchmark`; commit the generated `baseline-prof.txt` to `:app/src/main/`.
- Use `App Startup` library for initialiser ordering; move heavy SDK initialisations (Firebase, analytics) off the main thread using `InitializationProvider` with `WorkManagerInitializer` pattern.
- Avoid `Dispatchers.Main` for anything that doesn't need it. Profile with Android Studio Profiler and document two concrete optimisations in `ARCHITECTURE.md`.

---

### 10. Security Hardening

**Requirement:** Defence-in-depth for a production app.

- **Certificate pinning:** Pin the SHA-256 fingerprint of the server's leaf certificate in `OkHttpClient.Builder().certificatePinner(...)`. Handle `SSLPeerUnverifiedException` gracefully.
- **Network security config:** `res/xml/network_security_config.xml` — block all cleartext, pin the domain.
- **R8 full mode:** `minifyEnabled = true`, `shrinkResources = true` in release. `proguard-rules.pro` must preserve: Retrofit service interfaces, `kotlinx.serialization` models, Hilt-generated components, Room entities.
- **FLAG_SECURE:** Set on `MainActivity.window` in production builds so app content does not appear in recent-apps or be captured by screen-recording APIs.
- **No secrets in source:** All API keys, server URLs, and signing configs read from `local.properties` via `BuildConfig` fields — never committed.

---

## Local Database Schema (Room)

Define the following entities with foreign keys (`onDelete = CASCADE` unless stated), appropriate `@Index` annotations, and type converters for enums and lists.

```
┌─────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│      User       │    │    Workspace      │    │  WorkspaceMember │
│─────────────────│    │───────────────────│    │─────────────────│
│ id: String (PK) │◄───│ ownerId → User    │    │ workspaceId → W │
│ name            │    │ id: String (PK)   │◄───│ userId → User   │
│ email           │    │ name              │    │ role: MemberRole│
│ avatarUrl       │    │ slug              │    │ joinedAt        │
│ timezone        │    │ createdAt         │    └─────────────────┘
│ createdAt       │    └───────────────────┘
└─────────────────┘
         ▲
┌─────────────────┐    ┌───────────────────┐    ┌─────────────────┐
│    Project      │    │      Board        │    │      Task        │
│─────────────────│    │───────────────────│    │─────────────────│
│ id: String (PK) │    │ id: String (PK)   │    │ id: String (PK) │
│ workspaceId → W │    │ projectId → Proj  │    │ boardId → Board │
│ name            │    │ name              │    │ title           │
│ description     │    │ position: Int     │◄───│ descriptionJson │
│ color           │    └───────────────────┘    │ priority: Enum  │
│ dueDate: Long?  │                             │ position: Int   │
│ isArchived      │                             │ assigneeId→User │
│ createdAt       │                             │ dueDate: Long?  │
│ updatedAt       │                             │ syncStatus:Enum │
└─────────────────┘                             │ createdAt       │
                                                │ updatedAt       │
                                                └─────────────────┘

┌─────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐
│   SubTask   │  │    Label     │  │  TaskLabel   │  │    Comment      │
│─────────────│  │──────────────│  │──────────────│  │─────────────────│
│ id          │  │ id           │  │ taskId→Task  │  │ id              │
│ taskId→Task │  │ workspaceId  │  │ labelId→Lbl  │  │ taskId → Task   │
│ title       │  │ name         │  └──────────────┘  │ authorId → User │
│ isCompleted │  │ color        │                     │ body            │
└─────────────┘  └──────────────┘                     │ createdAt       │
                                                       └─────────────────┘

┌──────────────────┐  ┌──────────────┐  ┌─────────────────────────────────┐
│   Attachment     │  │ Notification │  │           SyncQueue             │
│──────────────────│  │──────────────│  │─────────────────────────────────│
│ id               │  │ id           │  │ id: Long (PK, autoGenerate)     │
│ taskId → Task    │  │ userId       │  │ entityType: String              │
│ name             │  │ type: Enum   │  │ entityId: String                │
│ localUri: String │  │ payloadJson  │  │ operation: CREATE/UPDATE/DELETE │
│ remoteUrl: String│  │ isRead       │  │ payloadJson: String             │
│ mimeType         │  │ createdAt    │  │ retryCount: Int                 │
│ sizeBytes: Long  │  └──────────────┘  │ createdAt: Long                 │
│ createdAt        │                    └─────────────────────────────────┘
└──────────────────┘

┌─────────────────────────────────────────────┐
│                  RemoteKey                  │
│─────────────────────────────────────────────│
│ entity: String  (e.g. "task:boardId")       │
│ prevKey: Int?                               │
│ nextKey: Int?                               │
└─────────────────────────────────────────────┘
```

**Enums:** `Priority { NONE, LOW, MEDIUM, HIGH, URGENT }` · `SyncStatus { SYNCED, PENDING, OPTIMISTIC, CONFLICT, FAILED }` · `MemberRole { OWNER, ADMIN, MEMBER }` · `NotificationType { ASSIGNED, MENTIONED, COMMENT, DUE_REMINDER }`

**Migrations:** Every schema change must have a corresponding Room `Migration` object. No `fallbackToDestructiveMigration()` in production builds.

---

## API Contract

### Base URLs

```
REST:      https://<your-server>/api/v1
WebSocket: wss://<your-server>/ws
```

### Authentication

All endpoints except `/auth/*` require:
```
Authorization: Bearer <access_token>
```
Access tokens expire in **15 minutes**. Refresh with `POST /auth/refresh`. Implement a `TokenAuthenticator` (`okhttp3.Authenticator`) that intercepts 401 responses, calls refresh transparently, and retries the original request — all without the caller knowing.

---

### REST Endpoints

#### Auth
```
POST   /auth/register           { name, email, password }
POST   /auth/login              { email, password }
                                → { accessToken, refreshToken, expiresIn, user }
POST   /auth/google             { idToken }
POST   /auth/refresh            { refreshToken } → { accessToken, expiresIn }
DELETE /auth/logout             { refreshToken }
```

#### Users
```
GET    /users/me
PATCH  /users/me                { name?, avatarUrl?, timezone? }
POST   /users/me/avatar         multipart/form-data  → { avatarUrl }
DELETE /users/me                (account deletion)
GET    /users/me/export         → 200 application/json  (data export)
```

#### Workspaces
```
GET    /workspaces
POST   /workspaces              { name }
GET    /workspaces/:id
PATCH  /workspaces/:id          { name? }
DELETE /workspaces/:id
GET    /workspaces/:id/members
POST   /workspaces/:id/invite   { email, role }
POST   /workspaces/:id/join     { inviteToken }
GET    /workspaces/:id/invite-link             → { url, qrCodeBase64 }
PATCH  /workspaces/:id/members/:userId        { role }
DELETE /workspaces/:id/members/:userId
```

#### Projects
```
GET    /workspaces/:wid/projects
POST   /workspaces/:wid/projects  { name, description, color, dueDate? }
GET    /projects/:id
PATCH  /projects/:id              { name?, description?, color?, dueDate?, isArchived? }
DELETE /projects/:id
GET    /projects/:id/analytics?from=<epoch>&to=<epoch>
```

Analytics response shape:
```json
{
  "completedByDay": [{ "date": "2025-05-01", "count": 7 }],
  "tasksByPriority": { "NONE": 3, "LOW": 8, "MEDIUM": 15, "HIGH": 6, "URGENT": 2 },
  "velocity": { "thisWeek": 14, "lastWeek": 11 },
  "overdueTasks": 4,
  "memberContributions": [{ "userId": "u1", "completed": 9 }]
}
```

#### Boards
```
GET    /projects/:pid/boards
POST   /projects/:pid/boards    { name, position }
PATCH  /boards/:id              { name?, position? }
DELETE /boards/:id
```

#### Tasks
```
GET    /boards/:bid/tasks
       ?page=1&limit=20&assigneeId=&priority=&labelId=&dueBefore=&search=
POST   /boards/:bid/tasks       { title, descriptionJson, priority, assigneeId?, dueDate?, position }
GET    /tasks/:id
PATCH  /tasks/:id               { title?, descriptionJson?, priority?, assigneeId?, dueDate? }
DELETE /tasks/:id
PATCH  /tasks/:id/move          { targetBoardId, position }
POST   /tasks/:id/block         { blockedByTaskId }
DELETE /tasks/:id/block/:blockId
```

#### Subtasks
```
GET    /tasks/:tid/subtasks
POST   /tasks/:tid/subtasks     { title }
PATCH  /subtasks/:id            { title?, isCompleted? }
DELETE /subtasks/:id
```

#### Comments
```
GET    /tasks/:tid/comments?page=1&limit=20
POST   /tasks/:tid/comments     { body }
PATCH  /comments/:id            { body }
DELETE /comments/:id
```

#### Attachments
```
POST   /tasks/:tid/attachments  multipart/form-data (field: "file")  → { attachment }
DELETE /attachments/:id
```

#### Labels
```
GET    /workspaces/:wid/labels
POST   /workspaces/:wid/labels  { name, color }
PATCH  /labels/:id              { name?, color? }
DELETE /labels/:id
POST   /tasks/:tid/labels       { labelId }
DELETE /tasks/:tid/labels/:lid
```

#### Notifications
```
GET    /notifications?page=1&limit=20&unreadOnly=false
PATCH  /notifications/:id/read
POST   /notifications/read-all
PATCH  /users/me/notification-prefs  { workspaceId, types: NotificationType[] }
```

#### Search
```
GET    /search?q=&workspaceId=&type=task|project|member&page=1&limit=20
```

---

### WebSocket Protocol

**Connection:** `wss://<your-server>/ws?token=<access_token>`

On connection, the server sends:
```json
{ "type": "connected", "payload": { "sessionId": "..." } }
```

**Subscribe to a project channel:**
```json
{ "action": "subscribe", "channel": "project:<projectId>" }
```
**Unsubscribe:**
```json
{ "action": "unsubscribe", "channel": "project:<projectId>" }
```

**Inbound event types (server → client):**

```json
{ "type": "task.created",    "payload": { /* Task object */ } }
{ "type": "task.updated",    "payload": { /* Task object */ } }
{ "type": "task.deleted",    "payload": { "id": "taskId" } }
{ "type": "task.moved",      "payload": { "id": "taskId", "boardId": "...", "position": 2 } }
{ "type": "comment.created", "payload": { /* Comment object */ } }
{ "type": "comment.deleted", "payload": { "id": "commentId", "taskId": "..." } }
{ "type": "member.joined",   "payload": { /* WorkspaceMember object */ } }
{ "type": "member.left",     "payload": { "userId": "...", "workspaceId": "..." } }
{ "type": "error",           "payload": { "code": "...", "message": "..." } }
```

**Heartbeat (client-initiated every 30 s):**
```json
{ "action": "ping" }
```
Server responds:
```json
{ "type": "pong", "timestamp": 1715000000000 }
```
If no pong within 10 s, close and reconnect with backoff.

---

## UI/UX Requirements

### Design System
- **Material 3** with Dynamic Colour on Android 12+; define a manual seed palette for older devices
- **Light / Dark / System** theme selection — theme stored in Proto DataStore, applied at `setContent {}`
- Typography scale defined as `MaterialTheme.typography` tokens in `:core:ui`
- All dimensions and spacings as named tokens (e.g. `NexusSpacing.Medium = 16.dp`) — no magic numbers
- **Edge-to-edge:** `WindowCompat.setDecorFitsSystemWindows(window, false)` in `MainActivity`; every scaffold must consume insets via `Modifier.windowInsetsPadding`

### Adaptive Layout
- Use `WindowSizeClass` to provide different layouts:
  - **Compact:** single-pane navigation (bottom bar)
  - **Medium:** bottom bar + wider content area
  - **Expanded (tablet/foldable):** `NavigationRail` + master-detail pane for board + task detail

### Accessibility
- All images and icons must have `contentDescription`
- Minimum touch target 48 × 48 dp
- All colour pairs must meet WCAG AA contrast ratio (4.5:1 for normal text)
- Support font scaling up to 200% without text truncation on primary content
- Test with TalkBack enabled — focus order must be logical

### Required Screens

| # | Screen | Key Requirements |
|---|---|---|
| 1 | Splash / Init | Show logo; initialise session; route to Login or Home |
| 2 | Login | Email/password + Google Sign-In button; biometric if available |
| 3 | Register | Name, email, password with strength indicator |
| 4 | Workspace List | Card grid; FAB to create; empty state |
| 5 | Create Workspace | Name input + slug preview |
| 6 | Members | List with roles; invite bottom sheet; QR code display |
| 7 | Project List | Coloured project cards; archive toggle |
| 8 | Create / Edit Project | Name, colour picker, date picker |
| 9 | Board (Kanban) | Horizontal scroll; drag-and-drop; real-time updates; offline badge |
| 10 | Task Detail | Full detail; subtasks; comments; attachments; activity log |
| 11 | Create / Edit Task | All fields; multi-select label picker |
| 12 | Search | Search bar (autofocus); chip filters; paginated results |
| 13 | Notification Centre | Grouped list; swipe-to-dismiss; mark-all-read |
| 14 | Analytics Dashboard | 4 chart cards; date range selector |
| 15 | Profile & Settings | Avatar; theme toggle; notification prefs; logout; delete account |

Every screen must implement **all four states:** loading (skeleton shimmer), content, empty, and error with a retry action.

---

## Testing Requirements

### Coverage Minimums (enforced in CI)

| Layer | Minimum Line Coverage |
|---|---|
| Domain — use cases | 90% |
| Data — repositories | 80% |
| Data — Room DAOs | 85% |
| Presentation — ViewModels | 80% |

### Unit Tests

Write unit tests for **all** of the following:

- Every use case: success path, empty result, network error, offline state
- Every ViewModel: state transitions from `Loading` → `Success` / `Error`; effect emission; `SavedStateHandle` restoration
- `TaskRemoteMediator`: all three `LoadType` values; verify Room upsert calls; verify `RemoteKey` writes
- `WebSocketManager`: reconnection backoff sequence; event parsing; subscription management
- `SyncWorker`: processes queue in order; retries on failure; marks FAILED after max retries
- `TokenAuthenticator`: intercepts 401; calls refresh; retries with new token; clears session on refresh failure
- Room DAO queries: use in-memory `Room.inMemoryDatabaseBuilder`; test filters, ordering, and pagination
- `ConflictResolver`: server-wins logic; correct snackbar event emission

Use **Turbine** for all `Flow`/`StateFlow` assertions.

### Instrumented / UI Tests

- Login flow: enter credentials → assert navigation to workspace list
- Create task → assert card appears on board
- Drag task to a different column → assert column label updates
- Go offline (disable network in test) → assert offline banner is visible
- Tap a simulated FCM deep link → assert task detail opens with correct task ID
- Biometric gate: app backgrounded > 5 min → assert lock screen composable is visible on resume

---

## Performance Requirements

| Metric | Target | How to Measure |
|---|---|---|
| Cold start → first frame | < 500 ms | `MacrobenchmarkRule` + `measureRepeated` |
| Board render (50 tasks) | < 300 ms | Composition trace in Android Studio Profiler |
| List fling frame time | < 16 ms (p95) | `FrameTimingMetric` in Macrobenchmark |
| Release APK size | < 25 MB | `./gradlew bundleRelease` + `bundleanalyzer` |
| Memory — board screen | < 150 MB heap | Memory Profiler snapshot |

### Required Benchmark Module (`:benchmark`)

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = rule.measureRepeated(
        packageName = "com.yourname.nexus",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

Also write `BoardScrollBenchmark` using `FrameTimingMetric` with `CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)`.

---

## Security Requirements

- [ ] `CertificatePinner` configured on `OkHttpClient` with the server's SHA-256 pin
- [ ] `network_security_config.xml`: `cleartextTrafficPermitted="false"`; certificate pin via `<pin-set>`
- [ ] `android:networkSecurityConfig` referenced in `AndroidManifest.xml`
- [ ] Auth tokens stored exclusively in `EncryptedSharedPreferences` (never logs, never `Bundle`)
- [ ] `FLAG_SECURE` set in all production Activities; release build only
- [ ] R8 full mode in release — provide working `proguard-rules.pro` (app must not crash on release build)
- [ ] Sensitive screens (Profile, Settings) re-prompt biometric/PIN if backgrounded > 5 min
- [ ] No secrets, API keys, signing configs, or server URLs in version control — all in `local.properties`
- [ ] `StrictMode` enabled in debug builds (`detectAll()`) — zero violations at runtime

---

## Build & CI/CD Requirements

### Gradle Setup

- **Version Catalogs:** All dependency coordinates and versions in `gradle/libs.versions.toml`
- **Convention Plugins:** Shared build logic extracted to `:build-logic` using the `includeBuild` approach. Provide at minimum:
  - `nexus.android.library` — common Android library config
  - `nexus.android.feature` — library + Hilt + Compose
  - `nexus.android.compose` — Compose compiler + options
  - `nexus.hilt` — Hilt plugin + dependency
- **No `buildSrc`** — use convention plugins only
- **Configuration cache** and **build cache** enabled in `gradle.properties`
- All modules use **Kotlin DSL** (`.gradle.kts`)
- Separate `debug` / `release` build types; `staging` build type pointing at a non-production API URL

### GitHub Actions (`.github/workflows/ci.yml`)

```
Triggers: push to main, all PRs

Jobs (run in parallel where possible):
  lint        → ./gradlew ktlintCheck lintDebug
  unit-tests  → ./gradlew testDebugUnitTest koverXmlReport
  build       → ./gradlew assembleRelease (with signing from GH secrets)
  ui-tests    → ./gradlew connectedDebugAndroidTest
               (use Gradle Managed Devices or emulator action)
```

- Fail the build if any test coverage threshold is breached (Kover plugin)
- Upload the release APK as a workflow artifact
- Cache Gradle and dependency files between runs

---

## Evaluation Criteria

| Criterion | Weight | What We Look For |
|---|---|---|
| Architecture & Module Design | 25% | Module separation, dependency direction, pattern correctness |
| Feature Completeness | 20% | All user stories implemented with all four UI states |
| Offline-First & Sync Quality | 15% | Correctness of sync queue, conflict resolution, optimistic UI |
| Code Quality & Readability | 15% | Naming, documentation, absence of anti-patterns |
| Testing Coverage & Quality | 10% | Coverage thresholds met, tests are meaningful (not trivial) |
| Performance | 10% | Benchmark targets met, Baseline Profiles included |
| Security Implementation | 5% | Pinning, encryption, obfuscation, FLAG_SECURE |

---

## Submission Guidelines

1. Push to a **private GitHub repository** (`nexus-android`) and invite the reviewer
2. Include an **`ARCHITECTURE.md`** covering:
   - Key architectural decisions and trade-offs considered
   - Why you chose your rich-text approach
   - How your conflict resolution works end-to-end
   - What you would do differently with more time
   - Any known limitations
3. Include a **3–5 minute screen recording** demonstrating:
   - Login (biometric if possible)
   - Create a task with rich-text description and an attachment
   - Drag the task between Kanban columns
   - Disable network → edit a task offline → re-enable network → confirm sync
   - Receive and tap a push notification to deep-link into a task
4. The release APK (R8-minified) must install and run without crashes

---

## Bonus Challenges

These are optional. Completing any of them is a strong positive signal.

| # | Challenge | Hint |
|---|---|---|
| B1 | **Glance Widget** — show today's tasks assigned to me on the home screen | `androidx.glance:glance-appwidget` |
| B2 | **Local full-text search** — search tasks in Room without a network call | Room FTS4 virtual table + `MATCH` query |
| B3 | **End-to-end encryption** — encrypt task description with AES-256-GCM before sending to server | Android Keystore + `Cipher` |
| B4 | **ML Kit Document Scanner** — scan a physical document and attach it as a PDF | `com.google.android.gms:play-services-mlkit-document-scanner` |
| B5 | **Voice-to-task** — long-press FAB to dictate a task title via `SpeechRecognizer` | `android.speech.SpeechRecognizer` |
| B6 | **Compose Multiplatform shared UI module** — move `:core:ui` to KMP, compile for Desktop | `org.jetbrains.compose` |
| B7 | **Custom Kanban columns** — allow adding, renaming, reordering, and deleting columns | Column management API endpoints are included |
| B8 | **Conflict diff viewer** — when server-wins overrides a local change, show a side-by-side diff | Custom composable, no library required |

---

*Good luck. Build something you'd be proud to ship.*
