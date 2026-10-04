plugins {
    id("echolist.compose.library")
    alias(libs.plugins.kotlinSerialization)
}
kotlin {
    sourceSets {
        all {
            if (name.endsWith("Test")) {
                languageSettings.optIn("io.kotest.common.ExperimentalKotest")
            }
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.driver.android)
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
        }
        commonMain.dependencies {
            implementation(project(":core:notifications"))
            implementation(project(":core:session"))
            implementation(project(":core:networking"))
            implementation(project(":core:database"))
            implementation(project(":core:files"))
            implementation(project(":core:protocol"))
            implementation(project(":core:designsystem"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)

            // Wire protobuf runtime
            implementation(libs.wire.runtime)

            // Ktor client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
            implementation(libs.ktor.client.mock)
        }
        jvmMain.dependencies {
            implementation(libs.icu4j)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.driver.jvm)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.sqldelight.driver.jvm)
            implementation(libs.koin.test)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.sqldelight.driver.js)
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.driver.js)
            }
        }
    }
}


compose.resources { packageOfResClass = "echolist.composeapp.generated.resources" }
