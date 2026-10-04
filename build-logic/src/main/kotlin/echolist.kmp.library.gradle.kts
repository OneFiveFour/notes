import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
kotlin {
    android {
        namespace = "net.onefivefour.echolist" + project.path.replace(':', '.')
        compileSdk = catalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = catalog.findVersion("android-minSdk").get().requiredVersion.toInt()
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        androidResources { enable = true }
    }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
    js { browser() }
    wasmJs { browser() }
    sourceSets {
        all {
            if (name.endsWith("Test")) languageSettings.optIn("io.kotest.common.ExperimentalKotest")
        }
        commonTest.dependencies {
            implementation(catalog.findLibrary("kotlin-test").get())
            implementation(catalog.findLibrary("kotlinx-coroutines-test").get())
            implementation(catalog.findLibrary("kotest-framework-engine").get())
            implementation(catalog.findLibrary("kotest-assertions-core").get())
            implementation(catalog.findLibrary("kotest-property").get())
        }
        jvmTest.dependencies { implementation(catalog.findLibrary("kotest-runner-junit5").get()) }
    }
}
tasks.withType<Test>().configureEach { useJUnitPlatform() }
