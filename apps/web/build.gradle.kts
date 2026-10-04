plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
kotlin {
    js { browser(); binaries.executable() }
    wasmJs { browser(); binaries.executable() }
    sourceSets.commonMain.dependencies {
        implementation(project(":app"))
        implementation(libs.compose.ui)
        implementation(libs.koin.core)
    }
}
