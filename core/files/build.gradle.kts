plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
}
