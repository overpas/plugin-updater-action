package by.overpass.plugin.updater.action

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual val httpClient: HttpClient = HttpClient(OkHttp)
