plugins { id("echolist.kmp.library") }
kotlin.sourceSets.getByName("commonMain").dependencies {
    implementation(project(":core:networking"))
    implementation(project(":core:protocol"))
    implementation(project(":core:files"))
    api(project(":core:notifications"))
    implementation(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.koin.core)
}