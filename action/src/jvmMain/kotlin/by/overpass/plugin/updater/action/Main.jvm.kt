package by.overpass.plugin.updater.action

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.github.z4kn4fein.semver.Version
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal actual val fileSystem: FileSystem = FileSystem.SYSTEM

fun main(args: Array<String>) = object : CliktCommand(
    name = "Plugin Updater",
    help = "Plugin Updater helps Intellij IDEA plugin developers to be notified when a new Intellij platform version" +
            " needs to be supported",
) {

    val product: String by option(
        names = arrayOf("--product"),
        help = "The name of the IDE",
    ).default("IntelliJ IDEA")

    val channelId: String by option(
        names = arrayOf("--channel-id"),
        help = "The IDE channel (EAP, RELEASE, etc.)",
    ).default("IC-IU-RELEASE-licensing-RELEASE")

    val buildNumberFormat: String by option(
        names = arrayOf("--build-number-format"),
        help = "Build number format",
    ).default("FULL")

    val untilBuildLocation: String by option(
        names = arrayOf("--until-build-location"),
        help = "Location of untilBuild value"
    ).default("src/build.gradle")

    val untilBuildProperty: String by option(
        names = arrayOf("--until-build-property"),
        help = "Name of the untilBuild property",
    ).default("plugin.until.build")

    override fun run() {
        runBlocking {
            checkForUpdates(
                product = product,
                channelId = channelId,
                buildNumberFormat = BuildNumberFormat.valueOf(buildNumberFormat),
                untilBuildLocation = /*untilBuildLocation*/ "../gradle.properties",
                untilBuildProperty = untilBuildProperty,
            )
        }
    }
}.main(args)

internal actual fun notifyUpdateAvailable(newBuildVersion: Version, currentBuild: Version) {
    println("A new IntelliJ Platform build available: $newBuildVersion\nCurrent is: $currentBuild")
}

internal actual fun notifyAllUpToDate() {
    println("The plugin supports the latest Intellij")
}

internal actual fun cwd(): Path {
    return System.getProperty("user.dir").toPath()
}

internal actual suspend fun tryUpdateCurrentBuild(
    untilBuildLocation: String,
    untilBuildProperty: String,
    latestBuild: Version,
) {
    // do nothing
}
