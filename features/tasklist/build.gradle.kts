plugins { id("echolist.compose.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":core:tasks"))
    implementation(project(":core:notifications"))
    implementation(project(":core:files"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.compose.materialIconsExtended)
}
kotlin.sourceSets.getByName("jvmMain").dependencies { implementation(libs.icu4j) }

compose.resources { packageOfResClass = "net.onefivefour.echolist.feature.tasklist.resources" }
