# Tasks: Network Request Logging

## Task 1: Create LogLevel enum and NetworkLoggingConfig

- [x] 1.1 Create `LogLevel` enum with `DEBUG`, `INFO`, `WARN`, `ERROR` values in `data/network/logging/LogLevel.kt`
- [x] 1.2 Create `NetworkLoggingConfig` class with `minLogLevel` property defaulting to `DEBUG` in `data/network/logging/NetworkLoggingConfig.kt`

## Task 2: Implement HexFormatter

- [x] 2.1 Create `HexFormatter` object in `data/network/logging/BodyFormatter.kt` with `format(bytes: ByteArray): String` function
- [x] 2.2 Implement empty body handling returning `"empty body"`
- [x] 2.3 Implement space-separated uppercase hex octet formatting with total byte count suffix
- [x] 2.4 Implement truncation at 1024 bytes with truncation suffix indicating total size
- [x] 2.5 Write property test for hex formatting correctness (Property 5) in `commonTest`
- [x] 2.6 Write unit tests for empty body, exact known outputs, and 1024/1025 byte boundary cases

## Task 3: Implement HeaderRedactor

- [x] 3.1 Create `HeaderRedactor` object in `data/network/logging/HeaderRedactor.kt` with `redact(headers: Map<String, List<String>>): Map<String, List<String>>` function
- [x] 3.2 Implement sensitive header identification: `Authorization` (case-insensitive) and header names containing `token` (case-insensitive)
- [x] 3.3 Replace sensitive header values with `[REDACTED]`, preserve non-sensitive headers unchanged
- [x] 3.4 Write property test for sensitive header identification (Property 3) in `commonTest`
- [x] 3.5 Write property test for sensitive values never appearing in redacted output (Property 4) in `commonTest`
- [x] 3.6 Write unit test for mixed sensitive and non-sensitive headers

## Task 4: Implement LogEntryFormatter

- [x] 4.1 Create `LogEntryFormatter` internal object in `data/network/logging/LogEntryFormatter.kt`
- [x] 4.2 Implement `formatRequest(method: String, url: String, headers: Map<String, List<String>>, body: ByteArray): String` using `HeaderRedactor` and `HexFormatter`
- [x] 4.3 Implement `formatResponse(statusCode: Int, headers: Map<String, List<String>>, body: ByteArray, elapsedMs: Long): String` using `HeaderRedactor` and `HexFormatter`
- [x] 4.4 Implement `formatError(url: String, errorMessage: String): String`
- [x] 4.5 Write property test for request log entry content (Property 1) in `commonTest`
- [x] 4.6 Write property test for response log entry content (Property 2) in `commonTest`
- [x] 4.7 Write property test for error entry content (Property 8) in `commonTest`

## Task 5: Implement NetworkLoggingPlugin

- [x] 5.1 Create `NetworkLoggingPlugin` using `createClientPlugin` in `data/network/logging/NetworkLoggingPlugin.kt`
- [x] 5.2 Implement `onRequest` hook: capture start time with `TimeSource.Monotonic`, format and emit request log entry via `println` when `DEBUG >= minLogLevel`
- [x] 5.3 Implement `on(Send)` hook: proceed with request, compute elapsed time, format and emit response log entry; assign WARN level for 400–599 status codes, ERROR level for exceptions/timeouts
- [x] 5.4 Wrap all logging in try-catch to ensure logging never breaks the HTTP pipeline
- [x] 5.5 Write property test for log level suppression (Property 6) in `jvmTest`
- [x] 5.6 Write property test for error status codes logged at WARN (Property 7) in `jvmTest`
- [x] 5.7 Write unit tests for plugin integration with `ktor-client-mock`: verify request/response logging, empty body, timeout error entry in `jvmTest`

## Task 6: Koin integration

- [x] 6.1 Install `NetworkLoggingPlugin` in the `HttpClient` block inside `networkModule` in `AppModules.kt`, alongside `AuthInterceptor` and `HttpTimeout`
- [x] 6.2 Configure default `minLogLevel` to `DEBUG`
