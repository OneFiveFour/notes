plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies { implementation(libs.androidx.core) }
    getByName("jsMain").dependencies { implementation(libs.kotlinx.browser) }
    getByName("wasmJsMain").dependencies { implementation(libs.kotlinx.browser) }
}
