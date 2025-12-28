plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "ch.hslu.newcmpproject"
version = "1.0.0"
application {
    mainClass.set("ch.hslu.newcmpproject.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)

    // Content Negotiation
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // SQLDelight
    implementation(libs.runtime)
    implementation(libs.jdbc.driver)
    implementation(libs.sqlDelight.jvm)

    // Web
    implementation(libs.ktor.server.cors)

    // Security
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

}