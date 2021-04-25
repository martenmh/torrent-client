import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.nio.charset.Charset

import java.util.*

import java.net.*
import java.nio.ByteBuffer

import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofByteArray

import kotlin.math.absoluteValue
import bencode.*
import kotlin.concurrent.thread
import peer.Peer
import utils.Log
import utils.*


enum class Event {
    STARTED,
    STOPPED,
    COMPLETED
}

var DEBUG = true

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    Log.timestamp = false

//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\sample.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\antiX-13.2_386-full.iso.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\elementsofcoordi00lone_archive.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\inferno00dant_2_archive.torrent")
    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\antiX-13.2_386-full.iso.torrent")
//    val torrentFile = File("H:\\TorrentClient\\src\\main\\resources\\linuxmint-18-cinnamon-64bit.iso.torrent")

    val input: ByteArray = readContentIntoByteArray(torrentFile)!!

    val decoder = BencodeDecoder()
    val output = decoder.decode(input)

    val encoder = BencodeEncoder()
    val info: BencodedData = (output as BencodedDictionary).get<BencodedDictionary>("info") as BencodedData

    val encodedInfo2 = decoder.getBencodedPart("info")
    val encodedInfo = encoder.encode(info)

    // 4f4d664043
    try {
        val info_hash = encodeURL(sha1Hex(encodedInfo2))

        val event: Event = Event.STARTED
        val peer_id = "kf99s08a09895s1s7642"
        val metaInfo = MetaInfo(output as BencodedDictionary)

        val client = HttpClient.newBuilder().build()
        val uriBase = URI(metaInfo.announce)

        val left = metaInfo.info.fileInfo.fold(
            { it.length },
            { it.files.map { file -> file.length }.reduce { a, b -> a + b } }
        )

        val uriString = "${metaInfo.announce}?" +
                "info_hash=${info_hash}&" +
                "peer_id=${peer_id}&" +
                "port=${uriBase.port}&" +
                "uploaded=${0}&" +
                "downloaded=${50}&" +
                "left=${left.absoluteValue}&" +
                "compact=${1}&" +
                "numwant=${50}&"
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

        Log.debug("", "Sending request to tracker.")
        val trackerResponse: HttpResponse<ByteArray> = client.send(trackerRequest, ofByteArray())


        val decodedResponse = decoder.decode(trackerResponse.body())
        val bytes = decoder.getBencodedPart("peers")

        var peerList = mutableListOf<Peer>()
        val peers = (decodedResponse as BencodedDictionary).value["peers"]
        val ha = "BitTorrent protocol"
        Log.debug("", "Byte representation size: ${ha.toByteArray(Charset.defaultCharset()).size}")
        val digest = sha1Hex(encodedInfo)!!.toByteArray(Charset.defaultCharset())
        val infoHash = sha1Hash(encodedInfo2)!!
        val handshake: ByteArray =
            byteArrayOf(19.toByte()) + ha.toByteArray(Charset.defaultCharset()) + ByteArray(8) + infoHash + peer_id.toByteArray(
                Charset.defaultCharset()
            )



        when (peers) {
            // binary
            is BencodedString -> {
                val peersSize = (peers).value.length / 6
                for (i in 0 until peersSize) {
                    val offset = i * 6
                    val portString = peers.value.substring((offset + 4) until (offset + 6))
                    val ip =
                        peers.value.substring((offset + 0) until (offset + 4)).toByteArray(Charset.defaultCharset())
                    val port: UInt = (portString[0].toByte().toUByte() * 256u) + portString[1].toByte().toUByte()
                    thread {
                        try {
                            val peer = Peer(ip = ip, port = port)
                            val peerHandshake = peer.handshake(handshake)
                            if (peerHandshake.contains(infoHash)) {
                                handlePeer(peer)
                            }
                        } catch (e: Exception) {
                            Log.warn("${InetAddress.getByAddress(ip).hostAddress}:${port}", "Dropping connection.")
                        }
                    }
                }

                Log.info("", "Peer size: ${peersSize}")
            }
            // dictionary
            is BencodedList -> {
                for (peer in peers.value) {
                    val peerDict = peer as BencodedDictionary
//                    peerDict.get("peer id")
//                    peerDict.get("ip")
//                    peerDict.get("port")
                }
            }
        }

        println("Now here!")

    } catch (e: UnexpectedByteException) {
        Log.error("", e.input.toString(Charset.defaultCharset()))
        Log.error("", e.toString())
//        e.printStackTrace()
    } catch (missingString: MissingRequiredStringException) {
        Log.error("", "Missing required string: '${missingString.message}' in ${torrentFile.path}")
//        missingString.printStackTrace()
    }
}

fun handlePeer(peer: Peer) {

    val tcpConnection = peer.peerConnection
    tcpConnection.onReceive {
        val peerHandshake = it

        Log.debug(peer.tag, "Received Peer message length: ${it.len}, messageID: ${it.id}")

        when (it.id.toInt()) {
            0 -> peer.peer_choking = true         // choke
            1 -> peer.peer_choking = false        // unchoke
            2 -> peer.peer_interested = true     // interested
            3 -> peer.peer_interested = false    // not interested
            4 -> {                          // have
                val pieceIndex = 0
            }
            5 -> {                          // bitfield
                Log.debug(
                    peer.tag,
                    "Got bitfield message with a length of ${it.payload.size * 8} bits (${it.payload.size} bytes), meaning the peer has ${it.payload.size * 8} pieces."
                )
                peer.bitfield = BitSet.valueOf(ByteBuffer.wrap(it.payload))

            }
            6 -> {                          // request
                // get 4 bytes for each int
                val index = 0
                val begin = 0
                val length = 0
                peer.onRequest(index, begin, length)
            }

            7 -> {  // piece
                val index = 0
                val begin = 0
                val block = ByteArray(0)
            }
            8 -> { // cancel
                Log.debug(peer.tag, "Received cancel message")
                // get 4 bytes for each int
                val index = 0
                val begin = 0
                val length = 0
            }
            9 -> {
                val listenPort: Short = 0
            }
        }

        peer.am_interested = true
        peer.am_choking = false

        if (!peer.peer_choking) {
            peer.request(0, 0, 1024)
        }
    }
//    tcpConnection.send(handshake)
    tcpConnection.recv()

}

private fun ByteArray.contains(element: ByteArray): Boolean {
    for (index in this.indices) {
        // find first byte
        if (this[index] == element[0]) {
            // check if all bytes correspond
            for (elementIndex in element.indices) {
                // if any byte does not correspond return false
                if (this[index + elementIndex] != element[elementIndex]) {
                    return false
                }
            }
            return true
        }
    }
    return false
}
