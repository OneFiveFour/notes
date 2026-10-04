plugins {
    id("echolist.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(project(":core:networking"))
    implementation(project(":core:protocol"))
    api(libs.koin.core)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies {
        implementation(libs.androidx.security.crypto)
        implementation(libs.androidx.core)
    }
    getByName("jvmMain").dependencies { implementation(project(":core:files")) }
    getByName("jsMain").dependencies { implementation(libs.kotlinx.browser) }
    getByName("wasmJsMain").dependencies { implementation(libs.kotlinx.browser) }
    getByName("commonTest").dependencies { implementation(libs.ktor.client.mock) }
}