package peer

import utils.TcpConnection

/**
 * A PeerConnection represents the TCP connection between a Peer and it's client.
 * Between the peer and client messages are passed of the type peer.PeerMessage in a binary format
 * @see PeerMessage
 */
class PeerConnection(ip: ByteArray, port: Int) : TcpConnection(ip, port) {
    private var onReceiveListeners: List<(PeerMessage) -> Unit> = listOf()

    fun send(data: PeerMessage) {
        send(data.toBytes())
    }

    fun recv() {
        val iss = socket.getInputStream()
        var bytesRead = 0
        while ((iss.read(buffer).also { bytesRead = it }) != -1) {
            for (listener in onReceiveListeners) {
                listener(PeerMessage(buffer))
            }
        }
    }

    fun onReceive(func: (PeerMessage) -> Unit) {
        val listeners = onReceiveListeners.toMutableList()
        listeners.add(func)
        onReceiveListeners = listeners.toList()
    }

}