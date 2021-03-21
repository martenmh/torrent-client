import java.io.File

/**
 * Searches this list or its range for an element having the key returned by the specified [selector] function
 * equal to the provided [key] value using the binary search algorithm.
 * The list is expected to be sorted into ascending order according to the Comparable natural ordering of keys of its elements.
 * otherwise the result is undefined.
 * * asdioasjdi
 *  * sajdioasjd
 * If the list contains multiple elements with the specified [key], there is no guarantee which one will be found.
 *
 * `null` value is considered to be less than any non-null value.
 *
 * @return the index of the element with the specified [key], if it is contained in the list within the specified range;
 * otherwise, the inverted insertion point `(-insertion point - 1)`.
 * The insertion point is defined as the index at which the element should be inserted,
 * so that the list (or the specified subrange of list) still remains sorted.
 * @sample samples.collections.Collections.Lists.binarySearchByKey
 */



fun main(args: Array<String>) {
    val b = object {
        var length: Int = 0        // length of the file in bytes
        var md5sum: String? = null // the (optional) MD5 sum of the file
    }
//    if (args.isEmpty())  = "tagalogenglishen00niggrich_archive.torrent"
    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\tagalogenglishen00niggrich_archive.torrent")
    val reader = torrentFile.reader()

    var input: String = reader.readLines().joinToString()
//    var input = Files.readAllLines(torrentFile.toPath(), Charset.defaultCharset())
    println(input)
    var decoder = BencodeDecoder()
    val output = decoder.decode(input)
    println(output)

//    val metaInfo = MetaInfo(output as BencodedDictionary)
//    println(metaInfo)

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
