plugins {
    id("echolist.kmp.library")
    alias(libs.plugins.sqldelight)
}
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.sqldelight.runtime)
    implementation(libs.koin.core)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies { implementation(libs.sqldelight.driver.android) }
    getByName("jvmMain").dependencies {
        implementation(libs.sqldelight.driver.jvm)
        implementation(project(":core:files"))
    }
    getByName("jsMain").dependencies { implementation(libs.sqldelight.driver.js); implementation(libs.kotlinx.browser) }
    getByName("wasmJsMain").dependencies { implementation(libs.sqldelight.driver.js); implementation(libs.kotlinx.browser) }
}
sqldelight {
    databases { create("EchoListDatabase") { packageName.set("net.onefivefour.echolist.cache") } }
}
