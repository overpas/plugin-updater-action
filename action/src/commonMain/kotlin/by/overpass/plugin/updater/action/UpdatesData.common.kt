package by.overpass.plugin.updater.action

import by.overpass.plugin.updater.action.BlogPost
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

internal expect val httpClient: HttpClient

@Serializable
data class Products(
    @XmlElement(true)
    @XmlSerialName("product")
    val products: List<Product>,
)

@Serializable
data class Product(
    @XmlSerialName("name")
    val name: String,
    @XmlElement(true)
    @XmlSerialName("code")
    val codes: List<String>,
    @XmlElement(true)
    @XmlSerialName("channel")
    val channels: List<Channel>,
)

@Serializable
data class Channel(
    @XmlSerialName("id")
    val id: String,
    @XmlSerialName("name")
    val name: String,
    @XmlSerialName("status")
    val status: String,
    @XmlSerialName("url")
    val url: String,
    @XmlSerialName("feedback")
    val feedback: String,
    @XmlSerialName("majorVersion")
    val majorVersion: String,
    @XmlSerialName("licensing")
    val licensing: String,
    @XmlElement(true)
    @XmlSerialName("build")
    val builds: List<Build>,
)

@Serializable
data class Build(
    @XmlSerialName("number")
    val number: String,
    @XmlSerialName("version")
    val version: String,
    @XmlSerialName("releaseDate")
    val releaseDate: String?,
    @XmlSerialName("fullNumber")
    val fullNumber: String?,
    @XmlElement(true)
    @XmlSerialName("blogPost")
    val blogPost: BlogPost?,
    @XmlElement(true)
    @XmlSerialName("message")
    val message: String?,
    @XmlElement(true)
    @XmlSerialName("button")
    val buttons: List<Button>?,
    @XmlElement(true)
    @XmlSerialName("patch")
    val patches: List<Patch>,
)

@Serializable
data class BlogPost(
    @XmlSerialName("url")
    val url: String,
)

@Serializable
data class Button(
    @XmlSerialName("name")
    val name: String,
    @XmlSerialName("url")
    val url: String,
    @XmlSerialName("download")
    val download: String?,
)

@Serializable
data class Patch(
    val from: String,
    val size: String,
    val fullFrom: String,
)

internal suspend fun getProductUpdates(): Products {
    return httpClient.get("https://www.jetbrains.com/updates/updates.xml")
        .body<String>()
        .let(XML::decodeFromString)
}
