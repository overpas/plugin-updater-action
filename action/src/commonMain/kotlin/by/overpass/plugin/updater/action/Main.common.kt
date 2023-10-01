package by.overpass.plugin.updater.action

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer

internal expect val fileSystem: FileSystem

internal enum class BuildNumberFormat {

    FULL,
    SHORT,
    HUMAN_READABLE,
}

internal suspend fun checkForUpdates(
    product: String,
    channelId: String,
    buildNumberFormat: BuildNumberFormat,
    untilBuildLocation: String,
    untilBuildProperty: String,
) {
    val productUpdates = getProductUpdates()
    val latestBuild = productUpdates.findLatestBuildFor(product, channelId).toVersion()
    val currentBuild = findCurrentBuild(untilBuildLocation, untilBuildProperty).toVersion()
    if (latestBuild > currentBuild) {
        notifyUpdateAvailable(latestBuild, currentBuild)
        tryUpdateCurrentBuild(untilBuildLocation, untilBuildProperty, latestBuild)
    } else {
        notifyAllUpToDate()
    }
}

internal fun Products.findLatestBuildFor(product: String, channelId: String): String {
    return products.find { it.name == product }
        ?.channels
        ?.find { it.id == channelId }
        ?.builds
        ?.maxBy { it.fullNumber?.toVersion() ?: it.number.toVersion() }
        ?.let {
            it.fullNumber ?: it.number
        } ?: throw IllegalStateException("Couldn't find latest build for $product, $channelId")
}

private fun findCurrentBuild(
    untilBuildLocation: String,
    untilBuildProperty: String,
): String {
    val (_, content) = getPathAndContent(untilBuildLocation)
    return when {
        untilBuildLocation.endsWith(".gradle") || untilBuildLocation.endsWith(".gradle.kts") -> {
            "untilBuild((.set\\()|(\\s*=\\s*))\"(?<version>.+)\"\\)?".toRegex()
                .find(content)
                ?.groups
                ?.get("version")
                ?.value ?: throw IllegalStateException("Couldn't determine the current untilBuild")
        }
        untilBuildLocation.endsWith(".properties") -> {
            "\\s*$untilBuildProperty\\s*=\\s*(.+)".toRegex()
                .find(content)
                ?.groupValues
                ?.get(1) ?: throw IllegalStateException("Couldn't determine the current untilBuild")
        }
        else -> throw IllegalStateException("Unknown untilBuildLocation: $untilBuildLocation")
    }
}

internal fun editFile(
    untilBuildLocation: String,
    untilBuildProperty: String,
    latestBuild: Version,
) {
    val (path, content) = getPathAndContent(untilBuildLocation)
    val newContent = when {
        untilBuildLocation.endsWith(".gradle") || untilBuildLocation.endsWith(".gradle.kts") -> {
            content.replace(
                "untilBuild\\s*=\\s*\"(?<version>.+)\"".toRegex(),
                "untilBuild = \"$latestBuild\"",
            ).replace(
                "untilBuild.set\\(\"(?<version>.+)\"\\)".toRegex(),
                "untilBuild.set(\"$latestBuild\")",
            )
        }
        untilBuildLocation.endsWith(".properties") -> {
            content.replace(
                "\\s*$untilBuildProperty\\s*=\\s*(.+)".toRegex(),
                "$untilBuildProperty=$latestBuild",
            )
        }
        else -> throw IllegalStateException("Unknown untilBuildLocation: $untilBuildLocation")
    }
    fileSystem.write(path) {
        writeUtf8(newContent)
    }
}

private fun getPathAndContent(untilBuildLocation: String): Pair<Path, String> {
    val child = untilBuildLocation.toPath()
    val parent = cwd()
    val path = parent.resolve(child)
    val content = fileSystem.source(path)
        .buffer()
        .readUtf8()
    return path to content
}

internal expect fun cwd(): Path

internal expect fun notifyUpdateAvailable(newBuildVersion: Version, currentBuild: Version)

internal expect fun notifyAllUpToDate()

internal expect suspend fun tryUpdateCurrentBuild(
    untilBuildLocation: String,
    untilBuildProperty: String,
    latestBuild: Version,
)
