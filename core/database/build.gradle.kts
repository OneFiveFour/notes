plugins {
    id("echolist.kmp.library")
    alias(libs.plugins.sqldelight)
}
kotlin {
    js { browser { testTask { useKarma { useChromeHeadless() } } } }
    wasmJs { browser { testTask { useKarma { useChromeHeadless() } } } }
}
kotlin.sourceSets.getByName("commonMain").dependencies {
    api(libs.sqldelight.runtime)
    api(libs.sqldelight.async)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.koin.core)
}

kotlin.sourceSets {
    getByName("androidMain").dependencies { implementation(libs.sqldelight.driver.android) }
    getByName("jvmMain").dependencies {
        implementation(libs.sqldelight.driver.jvm)
        implementation(project(":core:files"))
    }
    listOf("jsMain", "wasmJsMain").forEach { sourceSet ->
        getByName(sourceSet).dependencies {
            implementation(libs.sqldelight.driver.js)
            implementation(libs.kotlinx.browser)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqldelight.get()))
            implementation(npm("sql.js", libs.versions.sqljs.get()))
        }
    }
}
sqldelight {
    databases {
        create("EchoListDatabase") {
            packageName.set("net.onefivefour.echolist.cache")
            generateAsync.set(true)
        }
    }
}