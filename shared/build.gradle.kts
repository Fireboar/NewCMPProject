import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    //Database
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)

}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    js {
        browser()
    }

    sourceSets {
        all {
            //Database
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        commonMain.dependencies {
            //Database
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.runtime)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            //Database
            implementation(libs.ktor.client.android)
            implementation(libs.android.driver)
        }
        iosMain.dependencies {
            //Database
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)
        }
        jvmMain.dependencies {
            //Database
            implementation(libs.sqlDelight.jvm)
            implementation(libs.ktor.client.cio)
        }
        jsMain.dependencies {
            //Database
            implementation(libs.web.worker.driver)
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            //Database Worker
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.1.0"))
            implementation(npm("sql.js", "1.8.0"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}

android {
    namespace = "ch.hslu.newcmpproject.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("ch.hslu.cmpproject.cache")
            generateAsync.set(true)
        }
    }
}

