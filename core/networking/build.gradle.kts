plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.coroutines.core)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies { implementation(libs.ktor.client.okhttp) }
    getByName("jvmMain").dependencies { implementation(libs.ktor.client.okhttp) }
    getByName("jsMain").dependencies { implementation(libs.ktor.client.js) }
    getByName("wasmJsMain").dependencies { implementation(libs.ktor.client.js) }
    getByName("commonTest").dependencies { implementation(libs.ktor.client.mock); implementation(project(":core:protocol")) }
}
