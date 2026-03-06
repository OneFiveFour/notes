import org.gradle.kotlin.dsl.detektPlugins

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        toolVersion = "1.23.8"
        parallel = true
        buildUponDefaultConfig = true
        config.setFrom(files("${rootProject.projectDir}/detekt-config.yml"))
        source.setFrom(getDetektSourcePaths())
        ignoreFailures = false
        ignoredBuildTypes = listOf("staging", "release")
    }
}

private fun getDetektSourcePaths(): List<File> {
    val sourceDirs = mutableListOf<File>()

    subprojects.forEach {
        sourceDirs.add(file("${it.projectDir}/src/main/java"))
        sourceDirs.add(file("${it.projectDir}/src/test/java"))
        sourceDirs.add(file("${it.projectDir}/src/main/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/test/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/androidTest/java"))

        sourceDirs.add(file("${it.projectDir}/src/commonMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/androidMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/iosMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/desktopMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/wasmJsMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/jvmMain/kotlin"))
        sourceDirs.add(file("${it.projectDir}/src/nativeMain/kotlin"))
    }

    return sourceDirs.filter { it.exists() }
}

dependencies {
    detektPlugins("ru.kode:detekt-rules-compose:1.4.0")
}