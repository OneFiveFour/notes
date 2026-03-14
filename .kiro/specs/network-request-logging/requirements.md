# Requirements Document

## Introduction

EchoList communicates with its self-hosted backend over HTTP using protobuf-encoded messages via a custom ConnectRPC client built on Ktor. Currently, the networking layer provides no structured logging of request and response payloads. This feature adds detailed, security-conscious logging of outgoing requests and incoming responses to aid debugging and development. Because responses (and requests) are binary protobuf, the Logger displays binary payloads in hexadecimal format. Sensitive authentication data (JWT tokens, credentials) is redacted from all log output.

## Glossary

- **Logger**: A Ktor `HttpClient` plugin responsible for intercepting and logging HTTP request and response details
- **ConnectRpcClient**: The application's custom HTTP client interface that sends protobuf-encoded requests and receives protobuf-encoded responses over HTTP
- **Request_Log_Entry**: A structured log message emitted before an HTTP request is sent, containing method, URL, headers, and body
- **Response_Log_Entry**: A structured log message emitted after an HTTP response is received, containing status code, headers, body, and elapsed time
- **Sensitive_Header**: An HTTP header whose value contains authentication credentials — specifically the `Authorization` header and any header whose name contains "token" (case-insensitive)
- **Hex_Format**: A representation of binary data as a string of two-character hexadecimal octets (e.g., `0A 1B 2C`)
- **Log_Level**: The severity level of a log message — one of DEBUG, INFO, WARN, or ERROR
- **Redacted_Value**: The placeholder string `[REDACTED]` used in place of sensitive header values

## Requirements

### Requirement 1: Log Outgoing Requests

**User Story:** As a developer, I want to see detailed logs of every outgoing HTTP request, so that I can understand exactly what the app sends to the server.

#### Acceptance Criteria

1. WHEN the ConnectRpcClient sends an HTTP request, THE Logger SHALL emit a Request_Log_Entry at DEBUG Log_Level containing the HTTP method, the full request URL, and all non-sensitive request headers
2. WHEN the ConnectRpcClient sends an HTTP request with a non-empty body, THE Logger SHALL include the request body in Hex_Format in the Request_Log_Entry
3. WHEN the ConnectRpcClient sends an HTTP request with an empty body, THE Logger SHALL indicate "empty body" in the Request_Log_Entry
4. THE Logger SHALL emit the Request_Log_Entry before the HTTP request is transmitted to the server

### Requirement 2: Log Incoming Responses

**User Story:** As a developer, I want to see detailed logs of every server response, so that I can inspect what the server returned without needing external tools.

#### Acceptance Criteria

1. WHEN the ConnectRpcClient receives an HTTP response, THE Logger SHALL emit a Response_Log_Entry at DEBUG Log_Level containing the HTTP status code and all non-sensitive response headers
2. WHEN the ConnectRpcClient receives an HTTP response with a non-empty body, THE Logger SHALL include the response body in Hex_Format in the Response_Log_Entry
3. WHEN the ConnectRpcClient receives an HTTP response with an empty body, THE Logger SHALL indicate "empty body" in the Response_Log_Entry
4. THE Logger SHALL include the elapsed time in milliseconds between request transmission and response receipt in the Response_Log_Entry

### Requirement 3: Redact Sensitive Data

**User Story:** As a developer, I want authentication tokens and credentials excluded from logs, so that sensitive data is not exposed in log output.

#### Acceptance Criteria

1. WHEN a request or response contains a Sensitive_Header, THE Logger SHALL replace the header value with Redacted_Value in the log output
2. THE Logger SHALL identify Sensitive_Headers by matching the header name `Authorization` (case-insensitive) or any header name containing the substring "token" (case-insensitive)
3. THE Logger SHALL apply redaction before any log output is emitted — sensitive values SHALL NOT appear in log output under any circumstance

### Requirement 4: Hex Formatting of Binary Payloads

**User Story:** As a developer, I want binary protobuf payloads displayed in a readable hex format, so that I can inspect raw wire content without garbled output.

#### Acceptance Criteria

1. THE Logger SHALL format binary request and response bodies as space-separated two-character uppercase hexadecimal octets (e.g., `0A 1B 2C`)
2. WHEN a binary body exceeds 1024 bytes, THE Logger SHALL truncate the hex output at 1024 bytes and append a suffix indicating the total body size in bytes
3. THE Logger SHALL display the total body size in bytes alongside every hex-formatted payload

### Requirement 5: Cross-Platform Compatibility

**User Story:** As a developer, I want network logging to work on all target platforms, so that I can debug network issues regardless of which platform I am running.

#### Acceptance Criteria

1. THE Logger SHALL operate in Kotlin common code without any platform-specific (`expect`/`actual`) declarations
2. THE Logger SHALL function on all EchoList target platforms: Android, iOS, Desktop (JVM), Web (JS), and Web (WasmJS)
3. THE Logger SHALL use `println` as the log output sink to ensure compatibility across all Kotlin Multiplatform targets

### Requirement 6: Logger Integration as Ktor Plugin

**User Story:** As a developer, I want the logger installed as a Ktor HttpClient plugin, so that it intercepts all HTTP traffic automatically without modifying existing call sites.

#### Acceptance Criteria

1. THE Logger SHALL be implemented as a Ktor `HttpClient` plugin that intercepts requests and responses at the HTTP client level
2. WHEN the Logger plugin is installed on the HttpClient, THE Logger SHALL log all HTTP requests and responses made through that client without requiring changes to ConnectRpcClient or any call site
3. THE Logger SHALL be installed in the `networkModule` Koin module alongside the existing AuthInterceptor plugin

### Requirement 7: Configurable Log Level Threshold

**User Story:** As a developer, I want to control the minimum log level for network logging, so that I can reduce log noise in production builds.

#### Acceptance Criteria

1. THE Logger SHALL accept a configurable minimum Log_Level (DEBUG, INFO, WARN, ERROR) at plugin installation time
2. WHEN the configured minimum Log_Level is higher than DEBUG, THE Logger SHALL suppress Request_Log_Entry and Response_Log_Entry output
3. THE Logger SHALL default the minimum Log_Level to DEBUG when no explicit level is configured

### Requirement 8: Error Response Logging

**User Story:** As a developer, I want failed network requests logged at a higher severity, so that errors stand out in log output.

#### Acceptance Criteria

1. WHEN the ConnectRpcClient receives an HTTP response with a status code in the range 400–599, THE Logger SHALL emit the Response_Log_Entry at WARN Log_Level instead of DEBUG
2. WHEN a network request fails with an exception before a response is received, THE Logger SHALL emit a log entry at ERROR Log_Level containing the request URL and the exception message
3. IF a network request times out, THEN THE Logger SHALL emit a log entry at ERROR Log_Level containing the request URL and the timeout duration
