package by.overpass.plugin.updater.action

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual val httpClient: HttpClient = HttpClient(Js)
