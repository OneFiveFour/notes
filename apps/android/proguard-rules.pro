# EchoList ProGuard/R8 Rules

# --- Kotlin ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class net.onefivefour.echolist.**$$serializer { *; }
-keepclassmembers class net.onefivefour.echolist.** {
    *** Companion;
}
-keepclasseswithmembers class net.onefivefour.echolist.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Wire Protobuf ---
-keep class com.squareup.wire.** { *; }
-keepclassmembers class * extends com.squareup.wire.Message {
    <fields>;
    public static ** ADAPTER;
}
-keepclassmembers class * extends com.squareup.wire.WireEnum {
    public static ** ADAPTER;
}

# --- Ktor ---
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.** { *; }

# --- OkHttp ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Koin ---
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# --- SQLDelight ---
-keep class app.cash.sqldelight.** { *; }
-keep class net.onefivefour.echolist.cache.** { *; }

# --- Compose ---
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# --- App models (keep data classes used in serialization/network) ---
-keep class net.onefivefour.echolist.data.model.** { *; }
-keep class net.onefivefour.echolist.domain.** { *; }

# --- Google Tink / AndroidX Security Crypto ---
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.crypto.tink.**
-keep class com.google.crypto.tink.** { *; }

# --- General ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
