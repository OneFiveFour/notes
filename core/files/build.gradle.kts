plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
}
