package peer

import peer.PeerConnection
import peer.PeerMessage
import utils.toBytes
import java.util.*


class Peer(
    ip: ByteArray,
    port: UInt
) {
    val peerConnection: PeerConnection = PeerConnection(ip, port.toInt())
    val tag = "${peerConnection.endpoint().hostAddress}:${port}";

    var am_choking: Boolean = true         // this client is choking the peer
        set(value) {
            if (value) {
                peerConnection.send(PeerMessage(id = 0))
            } else {
                peerConnection.send(1.toBytes() + 1.toByte())
            }
            field = value
        }

    var am_interested: Boolean = false     // this client is interested in peer
        set(value) {
            if (value) {
                peerConnection.send(1.toBytes() + 0.toByte())
            } else {
                peerConnection.send(1.toBytes() + 1.toByte())
            }
            field = value
        }

    var peer_choking: Boolean = true       // peer is chocking this client
    var peer_interested: Boolean = false   // peer is interested in this client

    var keepAlive: Boolean = false

    /* Bitfield, specifies which pieces the peer has */
    var bitfield: BitSet? = null


    fun hasPiece(index: Int): Boolean {
        if (bitfield == null) {
            // send have request
            return false
        }
        return bitfield!!.get(index)
    }

    /** Request */
    fun request(index: Int, begin: Int, length: Int) {}
    fun onRequest(index: Int, begin: Int, length: Int) {}

    /**
     * Cancel request
     */
    fun cancel(index: Int, begin: Int, length: Int) {}

    fun piece(index: Int, begin: Int, block: ByteArray) {}

    fun handshake(handshake: ByteArray): ByteArray {
        peerConnection.send(handshake)
        return peerConnection.recvN(handshake.size)
    }

    fun sendInterested() {
        val message: ByteArray = 1.toBytes() + 3.toByte()
        peerConnection.send(message)
    }
}
