import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

compose.desktop {
    application {
        mainClass = "net.onefivefour.echolist.MainAppKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.onefivefour.echolist"
            packageVersion = "1.0.0"
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.onefivefour.echolist.MainAppKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.onefivefour.echolist"
            packageVersion = "1.0.0"
        }
    }
}
kotlin {
    jvm()
    sourceSets.jvmMain.dependencies {
        implementation(project(":app"))
        implementation(compose.desktop.currentOs)
        implementation(libs.koin.core)
    }
}