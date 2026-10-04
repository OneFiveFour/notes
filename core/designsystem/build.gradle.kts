plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.koin.core)
    implementation(libs.koin.compose)
    api(libs.kotlinx.coroutines.core)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.components.resources)
}
compose.resources {
    publicResClass = true
    packageOfResClass = "net.onefivefour.echolist.core.designsystem.resources"
}