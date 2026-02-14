# Koin Constructor DSL Migration Guide

## Why

Koin's `verify()` API checks that all dependencies in your DI graph are satisfied at test time. However, it works by inspecting **constructor parameters** via reflection. With lambda-based definitions (`single { MyClass(get()) }`), Koin can't see inside the lambda — so `verify()` silently passes them, giving a false sense of safety.

Switching to constructor DSL (`singleOf(::MyClass)`) lets `verify()` fully introspect every dependency, catching missing bindings before runtime.

## Current State

The `AppModules.kt` uses lambda-based definitions. Four implementation classes are `internal`, which prevents `singleOf` from referencing their constructors from the `di` package.

### Classes That Need Visibility Changes

| Class | File | Current | Constructor Params |
|---|---|---|---|
| `ConnectRpcClientImpl` | `network/client/ConnectRpcClientImpl.kt` | `internal` | `HttpClient`, `NetworkConfig` |
| `NetworkDataSourceImpl` | `data/source/network/NetworkDataSourceImpl.kt` | `internal` | `ConnectRpcClient` |
| `CacheDataSourceImpl` | `data/source/cache/CacheDataSourceImpl.kt` | `internal` | `NotesDatabase`, `() -> Long` (default) |
| `NotesRepositoryImpl` | `data/repository/NotesRepositoryImpl.kt` | `internal` | `NetworkDataSource`, `CacheDataSource`, `CoroutineDispatcher` (default) |

### Definitions That Stay Lambda-Based

These use builder/configuration logic that can't be expressed as a simple constructor call:

- `NetworkConfig` — uses `NetworkConfig.default(baseUrl = ...)` factory method
- `HttpClient` — uses Ktor's `HttpClient { install(HttpTimeout) { ... } }` builder

## Migration Steps

### 1. Remove `internal` from the four implementation classes

```kotlin
// Before
internal class ConnectRpcClientImpl(...)

// After
class ConnectRpcClientImpl(...)
```

Repeat for `NetworkDataSourceImpl`, `CacheDataSourceImpl`, `NotesRepositoryImpl`.

### 2. Update `AppModules.kt`

```kotlin
// Before
val networkModule: Module = module {
    single { NetworkConfig.default(baseUrl = "http://localhost:8080") }

    single {
        val config: NetworkConfig = get()
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
    }

    single<ConnectRpcClient> {
        ConnectRpcClientImpl(httpClient = get(), config = get())
    }

    single<NetworkDataSource> {
        NetworkDataSourceImpl(client = get())
    }
}

val dataModule: Module = module {
    single<CacheDataSource> {
        CacheDataSourceImpl(database = get())
    }

    single<NotesRepository> {
        NotesRepositoryImpl(
            networkDataSource = get(),
            cacheDataSource = get(),
            dispatcher = Dispatchers.Default
        )
    }
}
```

```kotlin
// After
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf

val networkModule: Module = module {
    // These stay lambda-based (builder/factory logic)
    single { NetworkConfig.default(baseUrl = "http://localhost:8080") }

    single {
        val config: NetworkConfig = get()
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
            }
        }
    }

    // These become constructor DSL
    singleOf(::ConnectRpcClientImpl) { bind<ConnectRpcClient>() }
    singleOf(::NetworkDataSourceImpl) { bind<NetworkDataSource>() }
}

val dataModule: Module = module {
    singleOf(::CacheDataSourceImpl) { bind<CacheDataSource>() }
    singleOf(::NotesRepositoryImpl) { bind<NotesRepository>() }
}
```

### 3. Update the verification test

After migration, `verify()` can fully introspect the constructor-based definitions. The `extraTypes` list may need adjustment:

```kotlin
@OptIn(KoinExperimentalAPI::class)
class KoinModuleVerificationTest : FunSpec({

    test("networkModule - all dependencies are satisfied") {
        networkModule.verify(
            extraTypes = listOf(
                io.ktor.client.engine.HttpClientEngine::class
            )
        )
    }

    test("dataModule - all dependencies are satisfied") {
        dataModule.verify(
            extraTypes = listOf(
                net.onefivefour.notes.cache.NotesDatabase::class
            )
        )
    }
})
```

If `verify()` complains about default parameters (like `CacheDataSourceImpl`'s `currentTimeMillis` or `NotesRepositoryImpl`'s `dispatcher`), add their types to `extraTypes`:

```kotlin
dataModule.verify(
    extraTypes = listOf(
        net.onefivefour.notes.cache.NotesDatabase::class,
        kotlin.coroutines.CoroutineContext::class,  // for dispatcher default
        Function0::class                             // for () -> Long default
    )
)
```

## Tradeoffs

| | Lambda DSL (current) | Constructor DSL (proposed) |
|---|---|---|
| `verify()` coverage | Partial — skips lambda definitions | Full — introspects all constructors |
| Visibility | Classes stay `internal` | Classes must be `public` |
| Readability | Explicit wiring visible in module | More concise, wiring is implicit |
| Flexibility | Can use builders, factories, conditionals | Constructor calls only |

## Recommendation

Migrate the four simple definitions to `singleOf`. Keep `NetworkConfig` and `HttpClient` as lambda-based since they require builder logic. This gives you full `verify()` coverage on the dependency chain that matters most (repository → data sources → network client) while keeping the flexibility where you need it.
