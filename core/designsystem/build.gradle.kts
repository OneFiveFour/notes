plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.coroutines.core)
}
compose.resources { packageOfResClass = "net.onefivefour.echolist.core.designsystem.resources" }
