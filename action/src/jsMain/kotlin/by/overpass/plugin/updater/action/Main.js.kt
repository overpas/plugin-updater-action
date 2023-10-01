package by.overpass.plugin.updater.action

import com.rnett.action.core.inputs
import com.rnett.action.core.outputs
import com.rnett.action.core.runAction
import com.rnett.action.exec.exec
import com.rnett.action.github.github
import io.github.z4kn4fein.semver.Version
import node.process.process
import okio.FileSystem
import okio.NodeJsFileSystem
import okio.Path
import okio.Path.Companion.toPath

suspend fun main() = runAction {
    val product = inputs.getRequired("product")
    val channelId = inputs.getRequired("channel-id")
    val buildNumberFormat = inputs.getRequired("build-number-format")
    val untilBuildLocation = inputs.getRequired("until-build-location")
    val untilBuildProperty = inputs.getRequired("until-build-property")
    checkForUpdates(
        product = product,
        channelId = channelId,
        buildNumberFormat = BuildNumberFormat.valueOf(buildNumberFormat),
        untilBuildLocation = untilBuildLocation,
        untilBuildProperty = untilBuildProperty,
    )
}

internal actual fun notifyUpdateAvailable(newBuildVersion: Version, currentBuild: Version) {
    outputs["new-build"] = newBuildVersion.toString()
    console.log("A new IntelliJ Platform build available: $newBuildVersion\n Current is: $currentBuild")
}

internal actual fun notifyAllUpToDate() {
    console.log("The plugin supports the latest Intellij")
}

internal actual val fileSystem: FileSystem = NodeJsFileSystem

internal actual fun cwd(): Path {
    return process.cwd().toPath()
}

internal actual suspend fun tryUpdateCurrentBuild(
    untilBuildLocation: String,
    untilBuildProperty: String,
    latestBuild: Version,
) {
    with(exec) {
        execShell("git checkout \"plugin-updater-${github.context.runId}\"")
        editFile(untilBuildLocation, untilBuildProperty, latestBuild)
        execShell("git add .")
        execShell("git commit -m \"Update untilBuild to $latestBuild\"")
        execShell("git push")
        execShell("gh pr create -B ${github.context.ref}")
    }
}
