# Requirements Document

## Introduction

This feature adds JWT-based authentication to the EchoList Compose Multiplatform app. The backend requires username/password login via ConnectRPC (protobuf) endpoints that return access and refresh tokens. The app needs a login screen, secure token storage with platform-specific implementations, automatic token refresh on 401 responses, and navigation gating so that unauthenticated users see only the login screen.

## Glossary

- **Auth_Service**: The ConnectRPC backend service exposing `Login` and `RefreshToken` RPCs as defined in the `auth.v1` protobuf package.
- **Login_Screen**: The Compose UI screen where the user enters backend URL, username, and password to authenticate.
- **Access_Token**: A short-lived JWT returned by Auth_Service, sent as a Bearer header on all authenticated requests.
- **Refresh_Token**: A longer-lived token returned by Auth_Service, used to obtain a new Access_Token without re-entering credentials.
- **Secure_Storage**: A platform-specific key-value store for persisting tokens. Uses EncryptedSharedPreferences on Android and localStorage/in-memory on JS/WasmJS.
- **Auth_Interceptor**: A Ktor HttpClient plugin that attaches the Access_Token to outgoing requests and handles automatic token refresh on 401 responses.
- **Network_Config**: The existing `NetworkConfig` data class that holds the base URL and timeout settings for the HTTP client.
- **Home_Screen**: The existing notes list screen (`HomeScreen`) that is shown after successful authentication.

## Requirements

### Requirement 1: Login Screen UI

**User Story:** As a user, I want to see a login screen when I open the app, so that I can enter my backend URL and credentials to authenticate.

#### Acceptance Criteria

1. WHEN the app launches and no valid Access_Token exists in Secure_Storage, THE Login_Screen SHALL display input fields for backend URL, username, and password.
2. WHEN the Login_Screen is displayed and a previously used backend URL exists in Secure_Storage, THE Login_Screen SHALL prefill the backend URL field with the stored value.
3. WHEN the user submits the login form with a blank backend URL, THE Login_Screen SHALL display a validation error indicating the backend URL is required.
4. WHEN the user submits the login form with a blank username, THE Login_Screen SHALL display a validation error indicating the username is required.
5. WHEN the user submits the login form with a blank password, THE Login_Screen SHALL display a validation error indicating the password is required.
6. WHEN the user submits the login form with valid inputs, THE Login_Screen SHALL display a loading indicator and disable the submit button until the login request completes.
7. THE Login_Screen SHALL use MaterialTheme.colorScheme, MaterialTheme.typography, MaterialTheme.shapes, and LocalEchoListDimensions for all styling.

### Requirement 2: Authentication via Auth_Service

**User Story:** As a user, I want to log in with my username and password, so that I receive tokens to access the backend.

#### Acceptance Criteria

1. WHEN the user submits valid credentials, THE Auth_Service client SHALL send a `LoginRequest` protobuf message containing the username and password to the `/auth.v1.AuthService/Login` endpoint.
2. WHEN Auth_Service returns a successful `LoginResponse`, THE system SHALL extract the access_token and refresh_token from the response.
3. WHEN Auth_Service returns a successful `LoginResponse`, THE system SHALL persist the access_token, refresh_token, and backend URL in Secure_Storage.
4. IF Auth_Service returns an error (invalid credentials, network failure, or timeout), THEN THE Login_Screen SHALL display a human-readable error message describing the failure.
5. WHEN the login request is in progress, THE Login_Screen SHALL prevent duplicate submissions.

### Requirement 3: Secure Token Storage

**User Story:** As a user, I want my tokens stored securely on my device, so that I remain authenticated across app restarts without exposing credentials.

#### Acceptance Criteria

1. THE Secure_Storage SHALL provide a common interface with `get(key: String): String?`, `put(key: String, value: String)`, and `delete(key: String)` operations.
2. WHEN running on Android, THE Secure_Storage SHALL use EncryptedSharedPreferences or a keystore-backed solution to store values.
3. WHEN running on JS or WasmJS, THE Secure_Storage SHALL use localStorage as a fallback storage mechanism.
4. WHEN `get` is called with a key that has no stored value, THE Secure_Storage SHALL return null.
5. WHEN `delete` is called, THE Secure_Storage SHALL remove the value so that a subsequent `get` for the same key returns null.
6. WHEN `put` is called with a key that already has a stored value, THE Secure_Storage SHALL overwrite the previous value with the new one.

### Requirement 4: Automatic Token Refresh

**User Story:** As a user, I want expired tokens to be refreshed automatically, so that I do not have to re-enter my credentials during a session.

#### Acceptance Criteria

1. WHEN an authenticated request receives a 401 HTTP response, THE Auth_Interceptor SHALL send a `RefreshTokenRequest` containing the stored refresh_token to the `/auth.v1.AuthService/RefreshToken` endpoint.
2. WHEN Auth_Service returns a successful `RefreshTokenResponse`, THE Auth_Interceptor SHALL update the stored access_token in Secure_Storage with the new value.
3. WHEN Auth_Service returns a successful `RefreshTokenResponse`, THE Auth_Interceptor SHALL retry the original failed request with the new access_token.
4. IF the refresh request itself fails (expired refresh_token, network error), THEN THE Auth_Interceptor SHALL clear all tokens from Secure_Storage and signal that re-authentication is required.
5. WHILE a token refresh is in progress, THE Auth_Interceptor SHALL queue concurrent 401-triggered requests and replay them after the refresh completes, rather than issuing multiple refresh requests simultaneously.

### Requirement 5: Bearer Token Attachment

**User Story:** As a developer, I want the access token automatically attached to every authenticated request, so that backend endpoints receive proper authorization headers.

#### Acceptance Criteria

1. WHEN an outgoing HTTP request is made to any endpoint other than `/auth.v1.AuthService/Login` and `/auth.v1.AuthService/RefreshToken`, THE Auth_Interceptor SHALL add an `Authorization: Bearer <access_token>` header using the token from Secure_Storage.
2. WHEN no access_token exists in Secure_Storage, THE Auth_Interceptor SHALL send the request without an Authorization header.

### Requirement 6: Navigation Gating

**User Story:** As a user, I want to be directed to the login screen when unauthenticated and to the home screen when authenticated, so that the app flow is seamless.

#### Acceptance Criteria

1. WHEN the app starts and a valid access_token exists in Secure_Storage, THE system SHALL navigate directly to Home_Screen, bypassing Login_Screen.
2. WHEN the app starts and no access_token exists in Secure_Storage, THE system SHALL show Login_Screen as the initial screen.
3. WHEN login completes successfully, THE system SHALL navigate from Login_Screen to Home_Screen.
4. WHEN the Auth_Interceptor signals that re-authentication is required (refresh failure), THE system SHALL navigate the user back to Login_Screen.

### Requirement 7: Backend URL Persistence

**User Story:** As a user, I want the app to remember the backend URL I last used, so that I do not have to re-enter it every time.

#### Acceptance Criteria

1. WHEN login completes successfully, THE system SHALL persist the backend URL in Secure_Storage.
2. WHEN the Login_Screen loads, THE system SHALL read the stored backend URL from Secure_Storage and prefill the input field.
3. WHEN a new backend URL is used for a successful login, THE system SHALL overwrite the previously stored backend URL.

### Requirement 8: Network Configuration Update

**User Story:** As a developer, I want the network layer to use the user-provided backend URL, so that the app connects to the correct server.

#### Acceptance Criteria

1. WHEN login completes successfully, THE system SHALL update Network_Config with the user-provided backend URL so that all subsequent API calls use the correct base URL.
2. WHEN the app starts with a stored backend URL and valid tokens, THE system SHALL initialize Network_Config with the stored backend URL.
