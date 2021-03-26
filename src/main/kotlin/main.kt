import java.io.File
import java.io.UnsupportedEncodingException
import java.lang.Exception

import java.nio.file.Files

import java.lang.StringBuilder
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import java.math.BigInteger
import kotlin.experimental.and

import kotlin.experimental.or

@Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
fun sha1Hex(input: ByteArray): String? {
    val md5 = MessageDigest.getInstance("SHA1")
    val digest = md5.digest(input)
    return digest.toHexString()
}

val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
fun bytesToHex(bytes: ByteArray): String? {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = (bytes[j] and 0xFF.toByte()).toInt()
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}

fun sha1Hash(toHash: String): String? {
    var hash: String? = null
    try {
        val digest = MessageDigest.getInstance("SHA-1")
        var bytes = toHash.toByteArray(charset("UTF-8"))
        digest.update(bytes, 0, bytes.size)
        return String.format("%040x", BigInteger(1, digest.digest()))
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return hash
}


/** Lookup table: character for a half-byte  */
val CHAR_FOR_BYTE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

/** Encode byte data as a hex string... hex chars are UPPERCASE */
fun encode(data: ByteArray?): String? {
    if (data == null || data.isEmpty()) {
        return ""
    }
    val store = CharArray(data.size * 2)
    for (i in data.indices) {
        val `val`: Int = (data[i] and 0xFF.toByte()).toInt()
        val charLoc = i shl 1
        store[charLoc] = CHAR_FOR_BYTE[`val` ushr 4]
        store[charLoc + 1] = CHAR_FOR_BYTE[`val` and 0x0F]
    }
    return String(store)
}

enum class Event {
    STARTED,
    STOPPED,
    COMPLETED
}

@Throws(Exception::class)
fun encodeURL(hexString: String?): String? {
    if (hexString == null || hexString.isEmpty) {
        return ""
    }
    if (hexString.length % 2 != 0) {
        throw Exception("String is not hex, length NOT divisible by 2: $hexString")
    }
    val len = hexString.length
    val output = CharArray(len + len / 2)
    var i = 0
    var j = 0
    while (i < len) {
        output[j++] = '%'
        output[j++] = hexString[i++]
        output[j++] = hexString[i++]
    }
    return String(output)
}

fun main(args: Array<String>) {

    println(sha1Hex("aff".toByteArray()))

//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\sample.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\antiX-13.2_386-full.iso.torrent")
    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\elementsofcoordi00lone_archive.torrent")

    val reader = torrentFile.reader()
    val input: ByteArray = Files.readAllBytes(torrentFile.toPath())

    reader.close()

    var decoder = BencodeDecoder(input)
    val encodedInfo = decoder.getBencodedPart("info")
    println("found encoded info: ${encodedInfo.toString(Charset.defaultCharset())}")
    val output = decoder.decode(input)
    println("decoded file")
    try {
        val info_hash = encodeURL(sha1Hex(encodedInfo))
        val event: Event = Event.STARTED
        val peer_id = "kf99s08a09895s1s7642"
        val metaInfo = MetaInfo(output as BencodedDictionary)

        val client = HttpClient.newBuilder().build()
        val uriBase = URI(metaInfo.announce)


        val uriString = "${metaInfo.announce}?info_hash=${info_hash}&" +
                "peer_id=${peer_id}&" +
                "port=${uriBase.port}&" +
                "uploaded=${0}&" +
                "downloaded=${0}&" +
                "left=${metaInfo.info.pieceLength}&" +
                "compact${0}&" +
                "no_peed_id&" +
                "event=${event.toString().toLowerCase()}"
        val trackerURI = URI(uriString)
        val scrapeURI = URI(uriString.replace("announce", "scrape"))
        // https://wiki.theory.org/BitTorrentSpecification
        val trackerRequest = HttpRequest.newBuilder()
            .GET()
            .uri(trackerURI)
            .build()

        val scrapeRequest = HttpRequest.newBuilder()
            .GET()
            .uri(scrapeURI)
            .build()

        println("Sending response!")
        val trackerResponse = client.send(trackerRequest, ofString())
        println("response: $trackerResponse")
        println(trackerResponse.headers())
        println(trackerResponse.body())

        val decodedResponse = decoder.decode(trackerResponse.body().toByteArray(Charset.defaultCharset()))
        println(decodedResponse)

    } catch (missingString: MissingRequiredStringException) {
        println("Missing required string: '${missingString.message}' in ${torrentFile.path}")
        println(missingString.data)
        missingString.printStackTrace()
        println()
    }
}
