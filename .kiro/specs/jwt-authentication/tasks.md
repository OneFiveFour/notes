# Implementation Plan: JWT Authentication

## Overview

Incrementally add JWT authentication to the EchoList app: protobuf definitions, secure storage, auth repository, login UI, Ktor auth interceptor, and navigation gating. Each step builds on the previous and wires into the existing architecture.

## Tasks

- [x] 1. Add auth protobuf definitions and Wire configuration
  - [x] 1.1 Create `proto/auth.proto` with the `auth.v1` service, `LoginRequest`, `LoginResponse`, `RefreshTokenRequest`, `RefreshTokenResponse` messages
    - _Requirements: 2.1_
  - [x] 1.2 Update `wire {}` block in `composeApp/build.gradle.kts` to prune `auth.v1.AuthService` (keep messages, skip gRPC stubs)
    - _Requirements: 2.1_

- [x] 2. Implement SecureStorage expect/actual
  - [x] 2.1 Create `expect class SecureStorage` in `commonMain` with `get(key)`, `put(key, value)`, `delete(key)` and `StorageKeys` object
    - _Requirements: 3.1_
  - [x] 2.2 Create `actual class SecureStorage` in `androidMain` using EncryptedSharedPreferences
    - Add `androidx.security:security-crypto` dependency to `androidMain.dependencies`
    - _Requirements: 3.2_
  - [x] 2.3 Create `actual class SecureStorage` in `jsMain` and `wasmJsMain` using `localStorage`
    - _Requirements: 3.3_
  - [x] 2.4 Create an in-memory `FakeSecureStorage` in `commonTest` for testing
    - _Requirements: 3.1_
  - [x] 2.5 Write property test for SecureStorage round-trip
    - **Property 7: SecureStorage put/get/delete round-trip**
    - Use `FakeSecureStorage` with `Arb.string()` for keys and values
    - Test all three sub-properties: put/get, put/delete/get, put/put/get
    - **Validates: Requirements 3.4, 3.5, 3.6**

- [x] 3. Implement AuthRepository
  - [x] 3.1 Create `AuthRepository` interface and `AuthRepositoryImpl` in `commonMain/data/repository/`
    - Implement `login(baseUrl, username, password)` using a temporary `HttpClient` + `ConnectRpcClient` for the user-provided URL
    - Implement `refreshToken()` using the stored base URL and refresh token
    - Implement `isAuthenticated()`, `clearAuth()`, `getAccessToken()`, `getBaseUrl()` backed by `SecureStorage`
    - _Requirements: 2.1, 2.2, 2.3, 4.1, 7.1_
  - [x] 3.2 Write property test for successful login persists credentials
    - **Property 5: Successful login persists all credentials**
    - Mock `ConnectRpcClient` to return generated token pairs, verify all three storage keys
    - **Validates: Requirements 2.2, 2.3, 7.1**
  - [x] 3.3 Write property test for login request construction
    - **Property 4: Login request construction**
    - Verify the request path and protobuf fields match provided username/password
    - **Validates: Requirements 2.1**

- [ ] 4. Checkpoint
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement AuthInterceptor as Ktor plugin
  - [ ] 5.1 Create `AuthInterceptor` Ktor client plugin in `commonMain/network/auth/`
    - Attach Bearer token on non-auth endpoints from `SecureStorage`
    - Handle 401 responses: acquire mutex, call `AuthRepository.refreshToken()`, store new token, retry original request
    - On refresh failure: call `AuthRepository.clearAuth()`, emit `AuthEvent.ReAuthRequired` via `MutableSharedFlow`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2_
  - [ ] 5.2 Update `networkModule` in `AppModules.kt` to install `AuthInterceptor` on the `HttpClient`
    - _Requirements: 5.1_
  - [ ]* 5.3 Write property test for Bearer token attachment
    - **Property 10: Bearer token attachment on non-auth endpoints**
    - Generate arbitrary non-auth paths and token strings, verify Authorization header
    - Use `ktor-client-mock` to inspect outgoing headers
    - **Validates: Requirements 5.1**
  - [ ]* 5.4 Write property test for 401 refresh-store-retry flow
    - **Property 8: 401 triggers refresh-store-retry**
    - Mock 401 response then successful refresh, verify token stored and request retried
    - **Validates: Requirements 4.1, 4.2, 4.3**
  - [ ]* 5.5 Write property test for refresh failure clears auth
    - **Property 9: Refresh failure clears auth**
    - Mock 401 then failed refresh, verify tokens cleared and ReAuthRequired emitted
    - **Validates: Requirements 4.4**

- [ ] 6. Implement LoginViewModel and LoginScreen UI
  - [ ] 6.1 Create `LoginViewModel` in `commonMain/ui/login/`
    - Define `LoginUiState` data class with field values, field errors, loading, and general error
    - Read stored backend URL from `SecureStorage` on init for prefill
    - Implement field change handlers and `onLoginClick` with blank-field validation
    - Call `AuthRepository.login()` and update state on success/failure
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 2.4, 2.5, 7.2_
  - [ ]* 6.2 Write property test for blank field validation
    - **Property 1: Blank field validation rejects submission**
    - Generate whitespace-only strings for each field, verify field error is set
    - **Validates: Requirements 1.3, 1.4, 1.5**
  - [ ]* 6.3 Write property test for backend URL prefill
    - **Property 2: Backend URL prefill round-trip**
    - Generate arbitrary URL strings, store in FakeSecureStorage, verify UiState.backendUrl
    - **Validates: Requirements 1.2, 7.2**
  - [ ]* 6.4 Write property test for loading state on valid submission
    - **Property 3: Valid submission triggers loading state**
    - Generate non-blank field triples, call onLoginClick, verify isLoading = true
    - **Validates: Requirements 1.6**
  - [ ]* 6.5 Write property test for error surfacing
    - **Property 6: Login errors surface to UI**
    - Generate various error results, verify UiState.error is non-null and isLoading is false
    - **Validates: Requirements 2.4**
  - [ ] 6.6 Create `LoginScreen` composable in `commonMain/ui/login/`
    - Stateless composable receiving `LoginUiState` and callbacks
    - Centered column with app title (titleLarge), three OutlinedTextFields, field error labels, login Button, general error text
    - Password field with `PasswordVisualTransformation`
    - Loading indicator and disabled button when `isLoading`
    - All styling via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`, `LocalEchoListDimensions.current`
    - _Requirements: 1.1, 1.6, 1.7, 2.5_

- [ ] 7. Implement AuthState and navigation gating
  - [ ] 7.1 Create `AuthViewModel` in `commonMain/ui/` with `AuthState` sealed interface
    - Check `SecureStorage` for access token on init → `Authenticated` or `Unauthenticated`
    - Collect `AuthEvent.ReAuthRequired` from SharedFlow → transition to `Unauthenticated`
    - Expose `authState: StateFlow<AuthState>`
    - _Requirements: 6.1, 6.2, 6.4_
  - [ ] 7.2 Create `LoginRoute` in `Routes.kt` and update `App.kt` navigation
    - Add `AuthState`-based branching: `Loading` → empty/splash, `Unauthenticated` → LoginScreen, `Authenticated` → existing NavDisplay
    - On successful login, transition AuthState to Authenticated
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  - [ ]* 7.3 Write property test for AuthState reflects storage
    - **Property 11: AuthState reflects storage**
    - Generate storage states with/without access token, verify AuthState
    - **Validates: Requirements 6.1, 6.2**

- [ ] 8. Implement dynamic NetworkConfig from backend URL
  - [ ] 8.1 Update `NetworkConfig` and `networkModule` to use stored backend URL from `SecureStorage`
    - Make `NetworkConfig` base URL mutable or re-create on login
    - On app startup with stored URL + tokens, initialize with stored URL
    - On login success, update config with user-provided URL
    - _Requirements: 8.1, 8.2_
  - [ ]* 8.2 Write property test for NetworkConfig URL
    - **Property 12: NetworkConfig uses correct backend URL**
    - Generate arbitrary URL strings, verify config update after login
    - **Validates: Requirements 8.1, 8.2**

- [ ] 9. Wire Koin DI modules
  - [ ] 9.1 Create `authModule` in `AppModules.kt` with all auth-related bindings
    - `SecureStorage`, `AuthRepository`, `MutableSharedFlow<AuthEvent>`, `AuthViewModel`, `LoginViewModel`
    - Add `authModule` to `appModules` list
    - Update `networkModule` to install `AuthInterceptor` and depend on auth bindings
    - _Requirements: 2.1, 3.1, 4.1, 5.1, 6.1_

- [ ] 10. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use Kotest Property (`checkAll` with 100+ iterations)
- Unit tests use `ktor-client-mock` and `FakeSecureStorage` for isolation
- iOS and Desktop `SecureStorage` actuals are deferred (not in scope)
