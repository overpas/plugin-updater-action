import com.rnett.action.generateAutoBuildWorkflow
import com.rnett.action.githubAction

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.github.action)
    alias(libs.plugins.kotlin.serialization)
}

group = properties["group"].toString()
version = properties["version"].toString()

kotlin {
    jvmToolchain(properties["jvm.version"].toString().toInt())
    js(IR) {
        githubAction()
    }
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.semver)
                implementation(libs.ktor.client.core)
                implementation(libs.okio)
                implementation(libs.xmlutil.core)
                implementation(libs.xmlutil.serialization)
                implementation(libs.xmlutil.serialutil)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlin.github.action)
                implementation(libs.ktor.client.js)
                implementation(libs.okio.nodefilesystem)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.clikt)
            }
        }
        val commonTest by getting {
            dependencies {
                kotlin("test")
            }
        }
    }
}

generateAutoBuildWorkflow(javaVersion = properties["jvm.version"].toString())
