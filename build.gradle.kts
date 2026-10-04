import org.gradle.kotlin.dsl.detektPlugins

plugins {
    id("echolist.architecture")
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    detekt {
        toolVersion = "1.23.8"
        parallel = true
        buildUponDefaultConfig = true
        config.setFrom(files("${rootProject.projectDir}/detekt-config.yml"))
        source.setFrom(getDetektSourcePaths())
        ignoreFailures = false
        ignoredBuildTypes = listOf("staging", "release")
    }

    dependencies {
        detektPlugins("ru.kode:detekt-rules-compose:1.4.0")
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.0.1")
        android.set(true)
        ignoreFailures.set(false)
    }

    // Exclude build directories from ktlint after evaluation
    afterEvaluate {
        tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
            exclude("**/build/**")
            exclude("**/generated/**")
        }
    }
}

private fun Project.getDetektSourcePaths(): List<File> =
    file("src").listFiles()?.map { it.resolve("kotlin") }?.filter { it.exists() }.orEmpty()