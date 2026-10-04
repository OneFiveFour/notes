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
    listOf("jsMain", "wasmJsMain").forEach { target ->
        sourceSets.getByName(target).dependencies {
            implementation(devNpm("copy-webpack-plugin", libs.versions.copyWebpackPlugin.get()))
        }
    }
}
