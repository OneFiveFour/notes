plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
}