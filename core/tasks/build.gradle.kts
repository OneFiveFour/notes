plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":core:networking"))
    implementation(project(":core:protocol"))
    implementation(project(":core:files"))
    implementation(project(":core:notifications"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.koin.core)
}
