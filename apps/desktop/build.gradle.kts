import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}
kotlin {
    jvm()
    sourceSets.jvmMain.dependencies {
        implementation(project(":app"))
        implementation(compose.desktop.currentOs)
        implementation(libs.koin.core)
    }
}
