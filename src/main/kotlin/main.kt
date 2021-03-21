import java.io.File
import java.lang.IllegalArgumentException

import java.nio.charset.Charset
import java.nio.file.Files
import java.io.ObjectInputStream

import java.util.zip.GZIPInputStream

import java.io.ByteArrayInputStream

import java.io.IOException

import java.io.ObjectOutputStream

import java.util.zip.GZIPOutputStream

import java.io.ByteArrayOutputStream


inline fun ByteArray.consumeWhile(predicate: (Char) -> Boolean): Pair<ByteArray, ByteArray> {
    for (index in this.indices) {
        if (!predicate(this[index].toChar())) {
            val consumed: ByteArray = this.copyOfRange(0, index)
            val rest: ByteArray = this.copyOfRange(index, this.size)
            return Pair(consumed, rest)
        }
    }
    return Pair(ByteArray(0), ByteArray(0))
}

inline fun String.consumeWhile(predicate: (Char) -> Boolean): Pair<String, String> {
    for (index in this.indices) {
        if (!predicate(this[index])) {
            val consumed = take(index)
            val rest = drop(index)
            return Pair(consumed, rest)
        }
    }
    return Pair("", "")
}

sealed class Either<out A, out B> {
    class Left<A>(val value: A) : Either<A, Nothing>()
    class Right<B>(val value: B) : Either<Nothing, B>()
}

inline fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
    when (this) {
        is Either.Left -> left(value)
        is Either.Right -> right(value)
    }

fun filePanic(notFoundStr: String) {
    throw IllegalArgumentException("Error, 'info' has not been found in the .torrent file")
}

/**
 * The meta info contains all useful info read from the .torrent file
 **/
class MetaInfo {
    class Info {
        data class SingleFileInfo(
            var name: String,  // filename
            var length: Int = 0,        // length of the file in bytes
            var md5sum: String? = null// the (optional) MD5 sum of the file)
        )

        data class FileInfo(
            var length: Int = 0,
            var md5sum: String? = null,
            var path: List<String> = listOf()
        )

        class MultipleFileInfo {
            var name: String
            var files: List<FileInfo>

            constructor(name: String, list: BencodedList) {
                this.name = name
                val fileList: MutableList<FileInfo> = mutableListOf()
                for (bencodedData in list.value) {
                    val dict = bencodedData as BencodedDictionary
                    fileList.add(
                        FileInfo(
                            length = dict.get<BencodedInt>("length")?.value ?: throw MissingRequiredString(
                                "length",
                                dict
                            ),
                            md5sum = dict.getString("md5"),
                            path = dict.getListOf<String>("path") ?: throw MissingRequiredString("path", dict)
                        )
                    )
                }
                files = fileList.toList()
            }
        }

        var fileInfo: Either<SingleFileInfo, MultipleFileInfo>
        var pieceLength: Int = 0    // number of bytes in each piece
        var pieces: String // concatenation of all 20-byte SHA1 hash values
        var private: Int? = null    // if set to 1 the client must publish its

        constructor(dict: BencodedDictionary?) {
            if (dict == null) throw MissingRequiredString("info")

            pieceLength =
                dict.get<BencodedInt>("piece length")?.value ?: throw MissingRequiredString("piece length", dict)
            pieces = dict.getString("pieces") ?: throw MissingRequiredString("pieces", dict)
            private = dict.get<BencodedInt>("private")?.value

            if (dict.get<BencodedList>("files") != null) {
                fileInfo = Either.Right(
                    MultipleFileInfo(
                        dict.getString("name")!!,
                        dict.get<BencodedList>("files")!!
                    )
                )
            } else {
                fileInfo = Either.Left(
                    SingleFileInfo(
                        dict.getString("name") ?: throw MissingRequiredString("name", dict),
                        dict.get<BencodedInt>("length")?.value ?: throw MissingRequiredString("length", dict),
                        dict.getString("md5")
                    )
                )
            }

        }
    }

    var info: Info
    var announce: String

    /** Optional metadata **/
    var announceList: List<String>? = null
    var creationDate: Int? = null
    var comment: String? = null
    var createdBy: String? = null
    var encoding: String? = null

    constructor(data: BencodedDictionary) {
        println(data)
        announce = data.getString("announce") ?: throw MissingRequiredString("announce", data)

        comment = data.getString("comment")
        createdBy = data.getString("created by")
        encoding = data.getString("encoding")
        announceList = data.getListOf<String>("key")
//        announceList = data.get<BencodedList>("announce-list").value.map {
//            if(it !is BencodedString) throw Exception("Expected each element to be a string, got ${it.javaClass}")
//            it.value as String
//        }

        creationDate = data.get<BencodedInt>("creation date")?.value
        println(data.get<BencodedDictionary>("info"))
        info = Info(data.get<BencodedDictionary>("info"))

    }
}

class MissingRequiredString(message: String, val data: BencodedData? = null) : Exception(message) {
}

fun main(args: Array<String>) {
//    if (args.isEmpty())  = "tagalogenglishen00niggrich_archive.torrent"
    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\elementsofcoordi00lone_archive.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\sample.torrent")
    val reader = torrentFile.reader()

    val input: ByteArray = Files.readAllBytes(torrentFile.toPath())

//    println("input size: ${input.length}, binary size: ${Files.readAllBytes(torrentFile.toPath()).size}")
    reader.close()
//    var input = Files.readAllLines(torrentFile.toPath(), Charset.defaultCharset())

    var decoder = BencodeDecoder()
    val output = decoder.decode(input)
    println(output)

    try {
        val metaInfo = MetaInfo(output as BencodedDictionary)
        println(metaInfo)
        println(metaInfo.info.pieceLength)
        println(metaInfo.info.fileInfo.fold(
            {
                println(it)

            },
            {
                println(it.name)
                println(it.files[0].length)

            }
        ))
    } catch (missingString: MissingRequiredString) {
        println("Missing required string: '${missingString.message}' in ${torrentFile.path}")
        println(missingString.data)
        missingString.printStackTrace()
        println()
    }
//    val client = HttpClient.newBuilder()
//        .version(HttpClient.Version.HTTP_1_1)
//        .build()
//
////        .followRedirects(HttpClient.Redirect.NORMAL)
////        .connectTimeout(Duration.ofSeconds(20))
////        .proxy(ProxySelector.of(InetSocketAddress("proxy.example.com", 80)))
////        .authenticator(Authenticator.getDefault())
////        .build()
//    val request = HttpRequest.newBuilder()
//        .uri(URI.create("http://foo.com/"))
//        .build()
//    client.sendAsync(request, BodyHandlers.ofString())
//        .thenApply { obj: HttpResponse<String?> -> obj.body() }
//        .thenAccept { x: String? -> println(x) }
//        .join()
//
//    val response = client.send(request, BodyHandlers.ofString())
//    println(response.statusCode())
//    println(response.body())
//
//    val uri = URI.create(metaInfo.announce)

//    println(output)
}
