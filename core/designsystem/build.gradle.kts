plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.coroutines.core)
}
compose.resources {
    publicResClass = true
    packageOfResClass = "net.onefivefour.echolist.core.designsystem.resources"
}