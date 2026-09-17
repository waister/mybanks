# Agent Guide: MyBanks Android App

This document provides essential information for LLM agents working on the MyBanks Android application codebase.

## 1. Project Overview

Android application that allows users to organize, view, edit, copy, and share their bank accounts, agency/account numbers, and PIX keys with automatic cloud backup, in-app update checks, and ad-supported monetization.

**Key Technologies:**

*   **Language:** Kotlin (Java 21 source/target)
*   **Build:** Android Gradle Plugin (AGP), Gradle Kotlin DSL, Version Catalog (`gradle/libs.versions.toml`); `compileSdk`/`targetSdk` 35, `minSdk` 23
*   **UI:** Jetpack Compose (BOM), Material 3, Navigation Compose (Single-Activity architecture)
*   **Architecture:** MVVM with Unidirectional Data Flow (UDF): `StateFlow` for state + `SharedFlow` for one-shot events
*   **Networking:** Retrofit + OkHttp + Gson (JSON). `BaseParamsInterceptor` appends the common query params (`identifier`, `identifier_old`, `version`, `platform`, `debug`, `api_v`) to every request.
*   **Local Storage:** Room (KSP / Coroutines / Flow) + SharedPreferences (`PreferencesRepository` in `data/repository/PreferencesRepository.kt`)
*   **Dependency Injection:** Koin
*   **Async:** Kotlin Coroutines & Flow
*   **Push Notifications:** Firebase Cloud Messaging (`MyFirebaseMessagingService`)
*   **Analytics/Crash:** Firebase Analytics, Firebase Crashlytics
*   **Ads:** Google AdMob (Adaptive Banner, Interstitial, Rewarded, and App Open Ads)
*   **Testing:** JUnit4, MockK, Robolectric, Turbine
*   **Code Quality:** ktlint (via `org.jlleitschuh.gradle.ktlint`), Kover coverage

## 2. Project Structure

Single-Activity app: `MainActivity.setContent { MyBanksTheme { AppNavHost() } }`.

Files at the root of `app/src/main/java/com/duduapps/mybanks/`:
*   **`application`**: `CustomApplication.kt` (Koin startup, MobileAds, AppOpenAdManager).
*   **`activity`**: `MainActivity.kt`.
*   **`data/local`**: Room database (`AppDatabase`), DAOs (`AccountDao`, `BankDao`), entities (`AccountEntity`, `BankEntity`).
*   **`data/repository`**: Repositories (`AccountRepository`, `BankRepository`, `AuthRepository`, `FeedbackRepository`, `AppConfigRepository`, `PreferencesRepository`), exposing `Flow` and `Result<T>`.
*   **`models`**: Domain data classes (`Account`, `Bank`) and Retrofit DTOs (`AccountsResponse`, `BanksResponse`, `IdentifyResponse`, `EmailCodeResponse`, `BaseApiResponse`).
*   **`di`**: Koin modules (`AppModule`, `LocalModule`, `NetworkModule`, `RepositoryModule`, `ViewModelModule`).
*   **`features`**: Self-contained feature packages:
    *   **`splash`**: `SplashScreen.kt`, `SplashViewModel.kt`, `SplashState.kt`
    *   **`main`**: `MainScreen.kt`, `MainViewModel.kt`, `MainState.kt`, `components/`
    *   **`account/detail`**: `AccountDetailScreen.kt`, `AccountDetailViewModel.kt`, `AccountDetailState.kt`
    *   **`account/form`**: `AccountFormScreen.kt`, `AccountFormViewModel.kt`, `AccountFormState.kt`
    *   **`auth`**: `LoginScreen.kt`, `LoginViewModel.kt`, `LoginState.kt`
    *   **`removeads`**: `RemoveAdsScreen.kt`, `RemoveAdsViewModel.kt`, `RemoveAdsState.kt`
    *   **`feedback`**: `FeedbackScreen.kt`, `FeedbackViewModel.kt`, `FeedbackState.kt`
*   **`navigation`**: `AppNavHost.kt`, `Routes.kt`.
*   **`network`**: Retrofit `ApiService.kt`, `BaseParamsInterceptor.kt`.
*   **`service`**: `MyFirebaseMessagingService.kt`.
*   **`ui/components`**: Shared composables — `AppTopBar.kt`, `AdMobBanner.kt`, `LoadingDialog.kt`, `ConfirmDialog.kt`.
*   **`ui/theme`**: `Color.kt`, `Theme.kt`, `Type.kt`.
*   **`utils`**: `AppOpenAdManager.kt`, `InterstitialAdManager.kt`.

## 3. Creating a New Feature

Features live under `app/src/main/java/com/duduapps/mybanks/features/<feature>/`. Follow the pattern of existing features:

*   **`<Feature>ViewModel.kt`**: Holds state (`MutableStateFlow<XxxUiState>` exposed as `StateFlow`), one-shot events (`MutableSharedFlow<XxxEvent>`), and `fun` methods for user actions. Injected repositories are called inside `viewModelScope.launch { ... }`.
*   **`<Feature>State.kt`**: `data class XxxUiState(...)` + `sealed interface XxxEvent`.
*   **`<Feature>Screen.kt`**: The main `@Composable`; collects `uiState`/`events` and renders using Material 3.
*   **`components/` (optional)**: Feature-specific `@Composable` components.

**State Management**
- All UI state that affects behavior lives in the ViewModel.
- State is exposed as a single `StateFlow<XxxUiState>`; one-shot messages via `SharedFlow<XxxEvent>`.
- Use `kotlinx.coroutines.flow.update { it.copy(...) }` to mutate state.
- Repository calls that can fail should return `Result<T>`, consumed with `.fold(onSuccess, onFailure)` or `.onSuccess { } / .onFailure { }`.

## 4. Dependency Injection (Koin)

Modules are in `app/src/main/java/com/duduapps/mybanks/di/`:

*   **`AppModule.kt`**: App-wide singletons.
*   **`LocalModule.kt`**: Room database, DAOs, `PreferencesRepository`.
*   **`NetworkModule.kt`**: Retrofit, OkHttp, `ApiService`, `BaseParamsInterceptor`.
*   **`RepositoryModule.kt`**: Repository implementations (`AccountRepository`, `BankRepository`, etc.).
*   **`ViewModelModule.kt`**: `viewModelOf(::XxxViewModel)` declarations.

Koin starts in `CustomApplication.onCreate()`. Test code must call `stopKoin()` in teardown to avoid `KoinApplicationAlreadyStartedException` (handled by `BaseRobolectricTest`).

## 5. Build & Test Commands

Run with `.\gradlew.bat` on Windows / `./gradlew` elsewhere:

```bash
# Build debug APK
./gradlew assembleDebug

# Compile Kotlin files
./gradlew compileDebugKotlin

# Run unit tests
./gradlew testDebugUnitTest

# Force re-run all unit tests
./gradlew testDebugUnitTest --rerun-tasks

# Check code style with ktlint
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Coverage verification
./gradlew koverVerify
```

## 6. Development Conventions

*   **Code Style:** ktlint with Android Studio style (max line 120, 4-space indent). Run `ktlintCheck` before submitting changes.
*   **Branching:** Create branches from `main`. All pull requests target `main`.
*   **Commits:** Follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification (e.g., `feat:`, `fix:`, `refactor:`).
*   **Dependencies:** All versions are defined in `gradle/libs.versions.toml`; reference by alias (e.g., `libs.androidx.room.runtime`) in `build.gradle.kts`. Never hardcode versions in build scripts.

## 7. Always-Rules

*   *Dependencies:* All dependencies are managed in `gradle/libs.versions.toml`. Use aliases in `build.gradle.kts` files.
*   *Compile verification:* After making changes to Kotlin source files (.kt), and before reporting the task complete, run `./gradlew compileDebugKotlin` to verify the project builds without errors.
*   *Code Style:* Run `./gradlew ktlintCheck` before submitting changes.
*   *Commits:* Follow the Conventional Commits specification.
